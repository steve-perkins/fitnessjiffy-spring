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
  Edit as EditIcon,
  Delete as DeleteIcon,
  Add as AddIcon,
  Search as SearchIcon,
} from '@mui/icons-material';
import { foodsApi } from '../api/foods';
import { useDate } from '../context/DateContext';
import DateSelector from '../components/DateSelector';
import {
  Food,
  FoodEaten,
  ServingType,
  SERVING_TYPE_LABELS,
  CreateFoodDto,
} from '../types';
import { calculateAllNutrition, formatNumber } from '../utils/calculations';

export default function FoodPage() {
  const queryClient = useQueryClient();
  const { dateString, displayDate } = useDate();

  // State
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<Food[]>([]);
  const [selectedRecentFood, setSelectedRecentFood] = useState<Food | null>(null);
  const [foodDialogOpen, setFoodDialogOpen] = useState(false);
  const [editingFood, setEditingFood] = useState<Food | null>(null);
  const [searchDialogOpen, setSearchDialogOpen] = useState(false);

  // Food form state
  const [foodForm, setFoodForm] = useState<CreateFoodDto>({
    name: '',
    defaultServingType: ServingType.OUNCE,
    servingTypeQty: 1,
    calories: 0,
    fat: 0,
    saturatedFat: 0,
    carbs: 0,
    fiber: 0,
    sugar: 0,
    protein: 0,
    sodium: 0,
  });

  // Queries
  const { data: foodsEaten = [], isLoading: foodsLoading } = useQuery({
    queryKey: ['foods-eaten', dateString],
    queryFn: () => foodsApi.getEaten({ date: dateString }),
  });

  const { data: recentFoods = [] } = useQuery({
    queryKey: ['recent-foods'],
    queryFn: foodsApi.getRecentlyEaten,
  });

  // Mutations
  const addFoodEatenMutation = useMutation({
    mutationFn: foodsApi.addEaten,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['foods-eaten'] });
      queryClient.invalidateQueries({ queryKey: ['recent-foods'] });
      queryClient.invalidateQueries({ queryKey: ['report-entries'] });
    },
  });

  const updateFoodEatenMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: { servingType?: ServingType; servingQty?: number } }) =>
      foodsApi.updateEaten(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['foods-eaten'] });
      queryClient.invalidateQueries({ queryKey: ['report-entries'] });
    },
  });

  const deleteFoodEatenMutation = useMutation({
    mutationFn: foodsApi.deleteEaten,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['foods-eaten'] });
      queryClient.invalidateQueries({ queryKey: ['report-entries'] });
    },
  });

  const createFoodMutation = useMutation({
    mutationFn: foodsApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['foods'] });
      setFoodDialogOpen(false);
    },
  });

  const updateFoodMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<CreateFoodDto> }) =>
      foodsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['foods'] });
      queryClient.invalidateQueries({ queryKey: ['foods-eaten'] });
      setFoodDialogOpen(false);
      setEditingFood(null);
    },
  });

  // Handlers
  const handleSearch = async () => {
    if (searchQuery.trim()) {
      const results = await foodsApi.search(searchQuery);
      setSearchResults(results);
      setSearchDialogOpen(true);
    }
  };

  const handleAddFood = (food: Food) => {
    addFoodEatenMutation.mutate({
      foodId: food.id,
      date: dateString,
      servingType: food.defaultServingType,
      servingQty: food.servingTypeQty,
    });
    setSearchDialogOpen(false);
    setSelectedRecentFood(null);
  };

  const handleAddRecentFood = () => {
    if (selectedRecentFood) {
      handleAddFood(selectedRecentFood);
    }
  };

  const handleUpdateFoodEaten = (
    foodEaten: FoodEaten,
    servingType: ServingType,
    servingQty: number
  ) => {
    updateFoodEatenMutation.mutate({
      id: foodEaten.id,
      data: { servingType, servingQty },
    });
  };

  const handleDeleteFoodEaten = (id: string) => {
    if (window.confirm('Are you sure you want to delete this food entry?')) {
      deleteFoodEatenMutation.mutate(id);
    }
  };

  const handleOpenFoodDialog = (food?: Food) => {
    if (food) {
      setEditingFood(food);
      setFoodForm({
        name: food.name,
        defaultServingType: food.defaultServingType,
        servingTypeQty: food.servingTypeQty,
        calories: food.calories,
        fat: food.fat,
        saturatedFat: food.saturatedFat,
        carbs: food.carbs,
        fiber: food.fiber,
        sugar: food.sugar,
        protein: food.protein,
        sodium: food.sodium,
      });
    } else {
      setEditingFood(null);
      setFoodForm({
        name: '',
        defaultServingType: ServingType.OUNCE,
        servingTypeQty: 1,
        calories: 0,
        fat: 0,
        saturatedFat: 0,
        carbs: 0,
        fiber: 0,
        sugar: 0,
        protein: 0,
        sodium: 0,
      });
    }
    setFoodDialogOpen(true);
  };

  const handleSaveFood = () => {
    if (editingFood) {
      updateFoodMutation.mutate({ id: editingFood.id, data: foodForm });
    } else {
      createFoodMutation.mutate(foodForm);
    }
  };

  // Calculate totals
  const totals = foodsEaten.reduce(
    (acc, fe) => {
      const nutrition = calculateAllNutrition(fe);
      return {
        calories: acc.calories + nutrition.calories,
        fat: acc.fat + nutrition.fat,
        saturatedFat: acc.saturatedFat + nutrition.saturatedFat,
        carbs: acc.carbs + nutrition.carbs,
        fiber: acc.fiber + nutrition.fiber,
        sugar: acc.sugar + nutrition.sugar,
        protein: acc.protein + nutrition.protein,
        sodium: acc.sodium + nutrition.sodium,
      };
    },
    { calories: 0, fat: 0, saturatedFat: 0, carbs: 0, fiber: 0, sugar: 0, protein: 0, sodium: 0 }
  );

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Food
      </Typography>

      {/* Date Selector */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <DateSelector />
          <Typography variant="subtitle1" color="text.secondary">
            Tracking food for {displayDate}
          </Typography>
        </CardContent>
      </Card>

      {/* Add Food Section */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Add Food
          </Typography>
          <Grid container spacing={2} alignItems="center">
            {/* Recent Foods */}
            <Grid item xs={12} md={4}>
              <Autocomplete
                options={recentFoods}
                getOptionLabel={(option) => option.name}
                value={selectedRecentFood}
                onChange={(_, value) => setSelectedRecentFood(value)}
                renderInput={(params) => (
                  <TextField {...params} label="Recently Eaten" size="small" />
                )}
              />
            </Grid>
            <Grid item xs={12} md={2}>
              <Button
                variant="contained"
                onClick={handleAddRecentFood}
                disabled={!selectedRecentFood}
                startIcon={<AddIcon />}
                fullWidth
              >
                Add
              </Button>
            </Grid>

            {/* Search Foods */}
            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                size="small"
                label="Search Foods"
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

            {/* Create New Food */}
            <Grid item xs={12}>
              <Button
                variant="text"
                onClick={() => handleOpenFoodDialog()}
                startIcon={<AddIcon />}
              >
                Create New Food
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Foods Eaten Table */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Foods Eaten Today
          </Typography>

          {foodsLoading ? (
            <CircularProgress />
          ) : foodsEaten.length === 0 ? (
            <Alert severity="info">No foods logged for this date.</Alert>
          ) : (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Food</TableCell>
                    <TableCell align="right"># Servings</TableCell>
                    <TableCell>Serving Size</TableCell>
                    <TableCell align="right">Calories</TableCell>
                    <TableCell align="right">Fat</TableCell>
                    <TableCell align="right">Sat. Fat</TableCell>
                    <TableCell align="right">Sodium</TableCell>
                    <TableCell align="right">Carbs</TableCell>
                    <TableCell align="right">Fiber</TableCell>
                    <TableCell align="right">Sugar</TableCell>
                    <TableCell align="right">Protein</TableCell>
                    <TableCell align="center">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {foodsEaten.map((fe) => (
                    <FoodEatenRow
                      key={fe.id}
                      foodEaten={fe}
                      onUpdate={handleUpdateFoodEaten}
                      onDelete={handleDeleteFoodEaten}
                      onEditFood={handleOpenFoodDialog}
                    />
                  ))}
                  {/* Totals Row */}
                  <TableRow sx={{ backgroundColor: 'action.hover' }}>
                    <TableCell colSpan={3}>
                      <strong>Totals</strong>
                    </TableCell>
                    <TableCell align="right">
                      <strong>{Math.round(totals.calories)}</strong>
                    </TableCell>
                    <TableCell align="right">
                      <strong>{formatNumber(totals.fat)}</strong>
                    </TableCell>
                    <TableCell align="right">
                      <strong>{formatNumber(totals.saturatedFat)}</strong>
                    </TableCell>
                    <TableCell align="right">
                      <strong>{formatNumber(totals.sodium)}</strong>
                    </TableCell>
                    <TableCell align="right">
                      <strong>{formatNumber(totals.carbs)}</strong>
                    </TableCell>
                    <TableCell align="right">
                      <strong>{formatNumber(totals.fiber)}</strong>
                    </TableCell>
                    <TableCell align="right">
                      <strong>{formatNumber(totals.sugar)}</strong>
                    </TableCell>
                    <TableCell align="right">
                      <strong>{formatNumber(totals.protein)}</strong>
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
            <Alert severity="info">No foods found matching your search.</Alert>
          ) : (
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell align="right">Serving</TableCell>
                    <TableCell align="right">Calories</TableCell>
                    <TableCell align="right">Fat</TableCell>
                    <TableCell align="right">Carbs</TableCell>
                    <TableCell align="center">Action</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {searchResults.map((food) => (
                    <TableRow key={food.id}>
                      <TableCell>
                        <Button
                          variant="text"
                          size="small"
                          onClick={() => {
                            setSearchDialogOpen(false);
                            handleOpenFoodDialog(food);
                          }}
                        >
                          {food.name}
                        </Button>
                      </TableCell>
                      <TableCell align="right">
                        {food.servingTypeQty} {SERVING_TYPE_LABELS[food.defaultServingType]}
                      </TableCell>
                      <TableCell align="right">{food.calories}</TableCell>
                      <TableCell align="right">{formatNumber(food.fat)}</TableCell>
                      <TableCell align="right">{formatNumber(food.carbs)}</TableCell>
                      <TableCell align="center">
                        <Button
                          variant="contained"
                          size="small"
                          onClick={() => handleAddFood(food)}
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

      {/* Food Edit/Create Dialog */}
      <Dialog
        open={foodDialogOpen}
        onClose={() => setFoodDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          {editingFood ? 'Edit Food' : 'Create New Food'}
        </DialogTitle>
        <DialogContent>
          {editingFood && !editingFood.ownerId && (
            <Alert severity="info" sx={{ mb: 2 }}>
              This is a global food. Saving will create a customized copy in your
              personal food list.
            </Alert>
          )}
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Name"
                value={foodForm.name}
                onChange={(e) =>
                  setFoodForm((prev) => ({ ...prev, name: e.target.value }))
                }
              />
            </Grid>
            <Grid item xs={6}>
              <FormControl fullWidth>
                <InputLabel>Serving Type</InputLabel>
                <Select
                  value={foodForm.defaultServingType}
                  onChange={(e: SelectChangeEvent<ServingType>) =>
                    setFoodForm((prev) => ({
                      ...prev,
                      defaultServingType: e.target.value as ServingType,
                    }))
                  }
                  label="Serving Type"
                >
                  {Object.values(ServingType).map((type) => (
                    <MenuItem key={type} value={type}>
                      {SERVING_TYPE_LABELS[type]}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                type="number"
                label="Serving Qty"
                value={foodForm.servingTypeQty}
                onChange={(e) =>
                  setFoodForm((prev) => ({
                    ...prev,
                    servingTypeQty: parseFloat(e.target.value) || 0,
                  }))
                }
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                type="number"
                label="Calories"
                value={foodForm.calories}
                onChange={(e) =>
                  setFoodForm((prev) => ({
                    ...prev,
                    calories: parseInt(e.target.value) || 0,
                  }))
                }
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                type="number"
                label="Fat (g)"
                value={foodForm.fat}
                onChange={(e) =>
                  setFoodForm((prev) => ({
                    ...prev,
                    fat: parseFloat(e.target.value) || 0,
                  }))
                }
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                type="number"
                label="Saturated Fat (g)"
                value={foodForm.saturatedFat}
                onChange={(e) =>
                  setFoodForm((prev) => ({
                    ...prev,
                    saturatedFat: parseFloat(e.target.value) || 0,
                  }))
                }
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                type="number"
                label="Carbs (g)"
                value={foodForm.carbs}
                onChange={(e) =>
                  setFoodForm((prev) => ({
                    ...prev,
                    carbs: parseFloat(e.target.value) || 0,
                  }))
                }
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                type="number"
                label="Fiber (g)"
                value={foodForm.fiber}
                onChange={(e) =>
                  setFoodForm((prev) => ({
                    ...prev,
                    fiber: parseFloat(e.target.value) || 0,
                  }))
                }
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                type="number"
                label="Sugar (g)"
                value={foodForm.sugar}
                onChange={(e) =>
                  setFoodForm((prev) => ({
                    ...prev,
                    sugar: parseFloat(e.target.value) || 0,
                  }))
                }
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                type="number"
                label="Protein (g)"
                value={foodForm.protein}
                onChange={(e) =>
                  setFoodForm((prev) => ({
                    ...prev,
                    protein: parseFloat(e.target.value) || 0,
                  }))
                }
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                type="number"
                label="Sodium (mg)"
                value={foodForm.sodium}
                onChange={(e) =>
                  setFoodForm((prev) => ({
                    ...prev,
                    sodium: parseFloat(e.target.value) || 0,
                  }))
                }
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setFoodDialogOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleSaveFood}
            disabled={createFoodMutation.isPending || updateFoodMutation.isPending}
          >
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

// Inline editing row component
interface FoodEatenRowProps {
  foodEaten: FoodEaten;
  onUpdate: (foodEaten: FoodEaten, servingType: ServingType, servingQty: number) => void;
  onDelete: (id: string) => void;
  onEditFood: (food: Food) => void;
}

function FoodEatenRow({ foodEaten, onUpdate, onDelete, onEditFood }: FoodEatenRowProps) {
  const [servingQty, setServingQty] = useState(foodEaten.servingQty.toString());
  const [servingType, setServingType] = useState(foodEaten.servingType);

  const nutrition = calculateAllNutrition(foodEaten);

  const handleUpdate = () => {
    const qty = parseFloat(servingQty);
    if (!isNaN(qty) && qty > 0) {
      onUpdate(foodEaten, servingType, qty);
    }
  };

  return (
    <TableRow>
      <TableCell>
        <Button
          variant="text"
          size="small"
          onClick={() => onEditFood(foodEaten.food)}
          sx={{ textAlign: 'left', justifyContent: 'flex-start' }}
        >
          {foodEaten.food.name}
        </Button>
      </TableCell>
      <TableCell align="right">
        <TextField
          type="number"
          size="small"
          value={servingQty}
          onChange={(e) => setServingQty(e.target.value)}
          onBlur={handleUpdate}
          inputProps={{ step: '0.1', min: '0.1', style: { width: '60px', textAlign: 'right' } }}
          variant="standard"
        />
      </TableCell>
      <TableCell>
        <Select
          size="small"
          value={servingType}
          onChange={(e) => {
            setServingType(e.target.value as ServingType);
            const qty = parseFloat(servingQty);
            if (!isNaN(qty) && qty > 0) {
              onUpdate(foodEaten, e.target.value as ServingType, qty);
            }
          }}
          variant="standard"
        >
          {Object.values(ServingType).map((type) => (
            <MenuItem key={type} value={type}>
              {SERVING_TYPE_LABELS[type]}
            </MenuItem>
          ))}
        </Select>
      </TableCell>
      <TableCell align="right">{Math.round(nutrition.calories)}</TableCell>
      <TableCell align="right">{formatNumber(nutrition.fat)}</TableCell>
      <TableCell align="right">{formatNumber(nutrition.saturatedFat)}</TableCell>
      <TableCell align="right">{formatNumber(nutrition.sodium)}</TableCell>
      <TableCell align="right">{formatNumber(nutrition.carbs)}</TableCell>
      <TableCell align="right">{formatNumber(nutrition.fiber)}</TableCell>
      <TableCell align="right">{formatNumber(nutrition.sugar)}</TableCell>
      <TableCell align="right">{formatNumber(nutrition.protein)}</TableCell>
      <TableCell align="center">
        <Tooltip title="Edit Food">
          <IconButton size="small" onClick={() => onEditFood(foodEaten.food)}>
            <EditIcon fontSize="small" />
          </IconButton>
        </Tooltip>
        <Tooltip title="Delete">
          <IconButton size="small" onClick={() => onDelete(foodEaten.id)} color="error">
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      </TableCell>
    </TableRow>
  );
}
