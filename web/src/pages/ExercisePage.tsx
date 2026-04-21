import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  Alert,
  CircularProgress,
  Autocomplete,
  Tooltip,
  SelectChangeEvent,
} from '@mui/material';
import {
  Delete as DeleteIcon,
  Add as AddIcon,
  Search as SearchIcon,
} from '@mui/icons-material';
import { exercisesApi } from '../api/exercises';
import { weightsApi } from '../api/weights';
import { useDate } from '../context/DateContext';
import DateSelector from '../components/DateSelector';
import { Exercise, ExercisePerformed } from '../types';
import { calculateExerciseCalories } from '../utils/calculations';

export default function ExercisePage() {
  const queryClient = useQueryClient();
  const { dateString, displayDate } = useDate();

  // State
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<Exercise[]>([]);
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedCategoryExercise, setSelectedCategoryExercise] = useState<Exercise | null>(null);
  const [selectedRecentExercise, setSelectedRecentExercise] = useState<Exercise | null>(null);
  const [searchDialogOpen, setSearchDialogOpen] = useState(false);

  // Queries
  const { data: exercisesPerformed = [], isLoading: exercisesLoading } = useQuery({
    queryKey: ['exercises-performed', dateString],
    queryFn: () => exercisesApi.getPerformed({ date: dateString }),
  });

  const { data: recentExercises = [] } = useQuery({
    queryKey: ['recent-exercises'],
    queryFn: exercisesApi.getRecentlyPerformed,
  });

  const { data: categories = [] } = useQuery({
    queryKey: ['exercise-categories'],
    queryFn: exercisesApi.getCategories,
  });

  const { data: exercisesInCategory = [] } = useQuery({
    queryKey: ['exercises-by-category', selectedCategory],
    queryFn: () => exercisesApi.getByCategory(selectedCategory),
    enabled: !!selectedCategory,
  });

  const { data: weightOnDate } = useQuery({
    queryKey: ['weight', dateString],
    queryFn: () => weightsApi.getOnDate(dateString),
  });

  const currentWeight = weightOnDate?.pounds || 150;

  // Mutations
  const addExerciseMutation = useMutation({
    mutationFn: exercisesApi.addPerformed,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['exercises-performed'] });
      queryClient.invalidateQueries({ queryKey: ['recent-exercises'] });
      queryClient.invalidateQueries({ queryKey: ['report-entries'] });
    },
  });

  const updateExerciseMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: { minutes: number } }) =>
      exercisesApi.updatePerformed(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['exercises-performed'] });
      queryClient.invalidateQueries({ queryKey: ['report-entries'] });
    },
  });

  const deleteExerciseMutation = useMutation({
    mutationFn: exercisesApi.deletePerformed,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['exercises-performed'] });
      queryClient.invalidateQueries({ queryKey: ['report-entries'] });
    },
  });

  // Handlers
  const handleSearch = async () => {
    if (searchQuery.trim()) {
      const results = await exercisesApi.search(searchQuery);
      setSearchResults(results);
      setSearchDialogOpen(true);
    }
  };

  const handleAddExercise = (exercise: Exercise, minutes = 30) => {
    addExerciseMutation.mutate({
      exerciseId: exercise.id,
      date: dateString,
      minutes,
    });
    setSearchDialogOpen(false);
    setSelectedRecentExercise(null);
    setSelectedCategoryExercise(null);
  };

  const handleAddRecentExercise = () => {
    if (selectedRecentExercise) {
      handleAddExercise(selectedRecentExercise);
    }
  };

  const handleAddCategoryExercise = () => {
    if (selectedCategoryExercise) {
      handleAddExercise(selectedCategoryExercise);
    }
  };

  const handleUpdateMinutes = (exercisePerformed: ExercisePerformed, minutes: number) => {
    if (minutes > 0) {
      updateExerciseMutation.mutate({
        id: exercisePerformed.id,
        data: { minutes },
      });
    }
  };

  const handleDeleteExercise = (id: string) => {
    if (window.confirm('Are you sure you want to delete this exercise entry?')) {
      deleteExerciseMutation.mutate(id);
    }
  };

  const handleCategoryChange = (e: SelectChangeEvent<string>) => {
    setSelectedCategory(e.target.value);
    setSelectedCategoryExercise(null);
  };

  // Calculate totals
  const totalMinutes = exercisesPerformed.reduce((sum, ep) => sum + ep.minutes, 0);
  const totalCalories = exercisesPerformed.reduce(
    (sum, ep) => sum + calculateExerciseCalories(ep, currentWeight),
    0
  );

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Exercise
      </Typography>

      {/* Date Selector */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <DateSelector />
          <Typography variant="subtitle1" color="text.secondary">
            Tracking exercise for {displayDate}
          </Typography>
        </CardContent>
      </Card>

      {/* Add Exercise Section */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Add Exercise
          </Typography>

          <Grid container spacing={2} alignItems="center">
            {/* Recently Performed */}
            <Grid item xs={12} md={4}>
              <Autocomplete
                options={recentExercises}
                getOptionLabel={(option) => option.description}
                value={selectedRecentExercise}
                onChange={(_, value) => setSelectedRecentExercise(value)}
                renderInput={(params) => (
                  <TextField {...params} label="Recently Performed" size="small" />
                )}
              />
            </Grid>
            <Grid item xs={12} md={2}>
              <Button
                variant="contained"
                onClick={handleAddRecentExercise}
                disabled={!selectedRecentExercise}
                startIcon={<AddIcon />}
                fullWidth
              >
                Add
              </Button>
            </Grid>

            {/* Browse by Category */}
            <Grid item xs={12} md={3}>
              <FormControl fullWidth size="small">
                <InputLabel>Category</InputLabel>
                <Select
                  value={selectedCategory}
                  onChange={handleCategoryChange}
                  label="Category"
                >
                  <MenuItem value="">
                    <em>Select a category</em>
                  </MenuItem>
                  {categories.map((category) => (
                    <MenuItem key={category} value={category}>
                      {category}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={3}>
              <Autocomplete
                options={exercisesInCategory}
                getOptionLabel={(option) =>
                  option.description.length > 50
                    ? option.description.substring(0, 50) + '...'
                    : option.description
                }
                value={selectedCategoryExercise}
                onChange={(_, value) => setSelectedCategoryExercise(value)}
                disabled={!selectedCategory}
                renderInput={(params) => (
                  <TextField {...params} label="Exercise" size="small" />
                )}
              />
            </Grid>
            <Grid item xs={12} md={2}>
              <Button
                variant="contained"
                onClick={handleAddCategoryExercise}
                disabled={!selectedCategoryExercise}
                startIcon={<AddIcon />}
                fullWidth
              >
                Add
              </Button>
            </Grid>

            {/* Search Exercises */}
            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                size="small"
                label="Search Exercises"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              />
            </Grid>
            <Grid item xs={12} md={2}>
              <Button
                variant="outlined"
                onClick={handleSearch}
                startIcon={<SearchIcon />}
                fullWidth
              >
                Search
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Exercises Performed Table */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Exercises Performed Today
          </Typography>

          {exercisesLoading ? (
            <CircularProgress />
          ) : exercisesPerformed.length === 0 ? (
            <Alert severity="info">No exercises logged for this date.</Alert>
          ) : (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Exercise</TableCell>
                    <TableCell align="right">Minutes</TableCell>
                    <TableCell align="right">Calories Burned</TableCell>
                    <TableCell align="center">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {exercisesPerformed.map((ep) => (
                    <ExercisePerformedRow
                      key={ep.id}
                      exercisePerformed={ep}
                      weight={currentWeight}
                      onUpdateMinutes={handleUpdateMinutes}
                      onDelete={handleDeleteExercise}
                    />
                  ))}
                  {/* Totals Row */}
                  <TableRow sx={{ backgroundColor: 'action.hover' }}>
                    <TableCell>
                      <strong>Totals</strong>
                    </TableCell>
                    <TableCell align="right">
                      <strong>{totalMinutes}</strong>
                    </TableCell>
                    <TableCell align="right">
                      <strong>{Math.round(totalCalories)}</strong>
                    </TableCell>
                    <TableCell />
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </CardContent>
      </Card>

      {/* Search Results Dialog */}
      <Dialog
        open={searchDialogOpen}
        onClose={() => setSearchDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Search Results</DialogTitle>
        <DialogContent>
          {searchResults.length === 0 ? (
            <Alert severity="info">No exercises found matching your search.</Alert>
          ) : (
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Description</TableCell>
                    <TableCell>Category</TableCell>
                    <TableCell align="right">MET</TableCell>
                    <TableCell align="center">Action</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {searchResults.map((exercise) => (
                    <TableRow key={exercise.id}>
                      <TableCell>{exercise.description}</TableCell>
                      <TableCell>{exercise.category}</TableCell>
                      <TableCell align="right">{exercise.metabolicEquivalent}</TableCell>
                      <TableCell align="center">
                        <Button
                          variant="contained"
                          size="small"
                          onClick={() => handleAddExercise(exercise)}
                        >
                          Add
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSearchDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

// Inline editing row component
interface ExercisePerformedRowProps {
  exercisePerformed: ExercisePerformed;
  weight: number;
  onUpdateMinutes: (ep: ExercisePerformed, minutes: number) => void;
  onDelete: (id: string) => void;
}

function ExercisePerformedRow({
  exercisePerformed,
  weight,
  onUpdateMinutes,
  onDelete,
}: ExercisePerformedRowProps) {
  const [minutes, setMinutes] = useState(exercisePerformed.minutes.toString());

  const caloriesBurned = calculateExerciseCalories(exercisePerformed, weight);

  const handleBlur = () => {
    const mins = parseInt(minutes);
    if (!isNaN(mins) && mins > 0 && mins !== exercisePerformed.minutes) {
      onUpdateMinutes(exercisePerformed, mins);
    }
  };

  return (
    <TableRow>
      <TableCell>{exercisePerformed.exercise.description}</TableCell>
      <TableCell align="right">
        <TextField
          type="number"
          size="small"
          value={minutes}
          onChange={(e) => setMinutes(e.target.value)}
          onBlur={handleBlur}
          inputProps={{ min: '1', style: { width: '60px', textAlign: 'right' } }}
          variant="standard"
        />
      </TableCell>
      <TableCell align="right">{Math.round(caloriesBurned)}</TableCell>
      <TableCell align="center">
        <Tooltip title="Delete">
          <IconButton
            size="small"
            onClick={() => onDelete(exercisePerformed.id)}
            color="error"
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      </TableCell>
    </TableRow>
  );
}
