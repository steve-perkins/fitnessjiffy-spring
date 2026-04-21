import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  CircularProgress,
  Divider,
  SelectChangeEvent,
} from '@mui/material';
import { format } from 'date-fns';
import { usersApi } from '../api/users';
import { weightsApi } from '../api/weights';
import { useDate } from '../context/DateContext';
import DateSelector from '../components/DateSelector';
import {
  Sex,
  ACTIVITY_LEVEL_LABELS,
  UpdateProfileDto,
} from '../types';
import {
  calculateBMI,
  calculateBMR,
  calculateMaintenanceCalories,
  calculateAge,
  formatHeight,
  formatNumber,
} from '../utils/calculations';

export default function ProfilePage() {
  const queryClient = useQueryClient();
  const { dateString } = useDate();

  // Fetch profile data
  const {
    data: profile,
    isLoading: profileLoading,
    error: profileError,
  } = useQuery({
    queryKey: ['profile'],
    queryFn: usersApi.getProfile,
  });

  // Fetch weight for selected date
  const { data: weightOnDate, isLoading: weightLoading } = useQuery({
    queryKey: ['weight', dateString],
    queryFn: () => weightsApi.getOnDate(dateString),
  });

  // Fetch all weights for finding current/latest weight
  const { data: allWeights } = useQuery({
    queryKey: ['weights'],
    queryFn: weightsApi.getAll,
  });

  // Form state for profile editing
  const [formData, setFormData] = useState<UpdateProfileDto>({});
  const [weightInput, setWeightInput] = useState('');
  const [saveError, setSaveError] = useState<string | null>(null);
  const [saveSuccess, setSaveSuccess] = useState(false);

  // Initialize form when profile loads
  useEffect(() => {
    if (profile) {
      setFormData({
        firstName: profile.firstName,
        lastName: profile.lastName,
        sex: profile.sex || undefined,
        birthdate: profile.birthdate || undefined,
        heightInInches: profile.heightInInches || undefined,
        activityLevel: profile.activityLevel || undefined,
        timezone: profile.timezone || undefined,
      });
    }
  }, [profile]);

  // Initialize weight input when weight data loads
  useEffect(() => {
    if (weightOnDate) {
      setWeightInput(weightOnDate.pounds.toString());
    } else {
      setWeightInput('');
    }
  }, [weightOnDate]);

  // Mutations
  const profileMutation = useMutation({
    mutationFn: (data: UpdateProfileDto) => usersApi.updateProfile(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profile'] });
      setSaveSuccess(true);
      setSaveError(null);
      setTimeout(() => setSaveSuccess(false), 3000);
    },
    onError: (error: Error) => {
      setSaveError(error.message);
      setSaveSuccess(false);
    },
  });

  const weightMutation = useMutation({
    mutationFn: async (pounds: number) => {
      // If weight exists for this date, update it; otherwise create new
      if (weightOnDate) {
        return weightsApi.update(weightOnDate.id, { pounds });
      } else {
        return weightsApi.create({ date: dateString, pounds });
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['weight'] });
      queryClient.invalidateQueries({ queryKey: ['weights'] });
      queryClient.invalidateQueries({ queryKey: ['report-entries'] });
    },
  });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSelectChange = (e: SelectChangeEvent<string | number>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSaveProfile = () => {
    profileMutation.mutate(formData);
  };

  const handleSaveWeight = () => {
    const pounds = parseFloat(weightInput);
    if (!isNaN(pounds) && pounds > 0) {
      weightMutation.mutate(pounds);
    }
  };

  // Calculate display values
  const latestWeight = allWeights?.[0]?.pounds || 0;
  const bmi =
    latestWeight && profile?.heightInInches
      ? calculateBMI(latestWeight, profile.heightInInches)
      : 0;
  const age = profile?.birthdate ? calculateAge(profile.birthdate) : 0;
  const bmr =
    latestWeight && profile?.heightInInches && age && profile?.sex
      ? calculateBMR(latestWeight, profile.heightInInches, age, profile.sex)
      : 0;
  const maintenanceCalories =
    bmr && profile?.activityLevel
      ? calculateMaintenanceCalories(bmr, profile.activityLevel)
      : 0;

  if (profileLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (profileError) {
    return (
      <Alert severity="error">
        Failed to load profile. Please try again later.
      </Alert>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Profile
      </Typography>

      {/* Weight Entry Section */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Record Weight
          </Typography>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} sm={4}>
              <DateSelector />
            </Grid>
            <Grid item xs={8} sm={4}>
              <TextField
                fullWidth
                type="number"
                label="Weight (lbs)"
                value={weightInput}
                onChange={(e) => setWeightInput(e.target.value)}
                inputProps={{ step: '0.1', min: '50', max: '1000' }}
                disabled={weightLoading}
                size="small"
              />
            </Grid>
            <Grid item xs={4} sm={4}>
              <Button
                variant="contained"
                onClick={handleSaveWeight}
                disabled={weightMutation.isPending || !weightInput}
              >
                {weightMutation.isPending ? 'Saving...' : 'Save'}
              </Button>
            </Grid>
          </Grid>
          {weightOnDate && (
            <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
              Last updated: {format(new Date(weightOnDate.date), 'MMMM d, yyyy')}
            </Typography>
          )}
        </CardContent>
      </Card>

      {/* Profile Information Section */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Personal Information
          </Typography>

          {saveError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {saveError}
            </Alert>
          )}
          {saveSuccess && (
            <Alert severity="success" sx={{ mb: 2 }}>
              Profile updated successfully!
            </Alert>
          )}

          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Email"
                value={profile?.email || ''}
                disabled
                size="small"
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="First Name"
                name="firstName"
                value={formData.firstName || ''}
                onChange={handleInputChange}
                size="small"
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Last Name"
                name="lastName"
                value={formData.lastName || ''}
                onChange={handleInputChange}
                size="small"
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth size="small">
                <InputLabel>Sex (for metabolic calculations)</InputLabel>
                <Select
                  name="sex"
                  value={formData.sex || ''}
                  onChange={handleSelectChange}
                  label="Sex (for metabolic calculations)"
                >
                  <MenuItem value={Sex.MALE}>Male</MenuItem>
                  <MenuItem value={Sex.FEMALE}>Female</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                type="date"
                label="Birthdate"
                name="birthdate"
                value={
                  formData.birthdate
                    ? format(new Date(formData.birthdate), 'yyyy-MM-dd')
                    : ''
                }
                onChange={(e) => {
                  setFormData((prev) => ({
                    ...prev,
                    birthdate: e.target.value
                      ? new Date(e.target.value).toISOString()
                      : undefined,
                  }));
                }}
                InputLabelProps={{ shrink: true }}
                size="small"
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                type="number"
                label="Height (inches)"
                name="heightInInches"
                value={formData.heightInInches || ''}
                onChange={(e) => {
                  setFormData((prev) => ({
                    ...prev,
                    heightInInches: e.target.value
                      ? parseInt(e.target.value)
                      : undefined,
                  }));
                }}
                inputProps={{ min: 36, max: 96 }}
                helperText={
                  formData.heightInInches
                    ? formatHeight(formData.heightInInches)
                    : ''
                }
                size="small"
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth size="small">
                <InputLabel>Activity Level</InputLabel>
                <Select
                  name="activityLevel"
                  value={formData.activityLevel || ''}
                  onChange={handleSelectChange}
                  label="Activity Level"
                >
                  {Object.entries(ACTIVITY_LEVEL_LABELS).map(([value, label]) => (
                    <MenuItem key={value} value={parseFloat(value)}>
                      {label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <Button
                variant="contained"
                onClick={handleSaveProfile}
                disabled={profileMutation.isPending}
              >
                {profileMutation.isPending ? 'Saving...' : 'Save Profile'}
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Calculated Stats Section */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Calculated Statistics
          </Typography>
          <Divider sx={{ mb: 2 }} />
          <Grid container spacing={2}>
            <Grid item xs={6} sm={3}>
              <Typography variant="body2" color="text.secondary">
                Current Weight
              </Typography>
              <Typography variant="h6">
                {latestWeight ? `${formatNumber(latestWeight)} lbs` : '-'}
              </Typography>
            </Grid>
            <Grid item xs={6} sm={3}>
              <Typography variant="body2" color="text.secondary">
                BMI
              </Typography>
              <Typography variant="h6">
                {bmi ? formatNumber(bmi, 2) : '-'}
              </Typography>
            </Grid>
            <Grid item xs={6} sm={3}>
              <Typography variant="body2" color="text.secondary">
                Maintenance Calories
              </Typography>
              <Typography variant="h6">
                {maintenanceCalories
                  ? maintenanceCalories.toLocaleString()
                  : '-'}
              </Typography>
            </Grid>
            <Grid item xs={6} sm={3}>
              <Typography variant="body2" color="text.secondary">
                Age
              </Typography>
              <Typography variant="h6">{age || '-'}</Typography>
            </Grid>
          </Grid>
        </CardContent>
      </Card>
    </Box>
  );
}
