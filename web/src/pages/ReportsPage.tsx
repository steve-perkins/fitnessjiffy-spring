import { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Box,
  Card,
  CardContent,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  CircularProgress,
  Alert,
  Divider,
  SelectChangeEvent,
} from '@mui/material';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { format, parseISO, subMonths, subYears, isAfter } from 'date-fns';
import { reportsApi } from '../api/reports';
import { formatNumber } from '../utils/calculations';

type DateRange = 'all' | 'month' | 'year';
type DataType = 'weight' | 'netCalories' | 'weight30avg' | 'calories30avg';

const DATA_TYPE_LABELS: Record<DataType, string> = {
  weight: 'Weight (lbs)',
  netCalories: 'Net Calories per Day',
  weight30avg: 'Weight 30-day Moving Avg',
  calories30avg: 'Calories 30-day Moving Avg',
};

const DATA_TYPE_COLORS: Record<DataType, string> = {
  weight: '#FF6600',
  netCalories: '#FCD202',
  weight30avg: '#2196F3',
  calories30avg: '#4CAF50',
};

export default function ReportsPage() {
  const [dateRange, setDateRange] = useState<DateRange>('month');
  const [leftAxis, setLeftAxis] = useState<DataType>('weight');
  const [rightAxis, setRightAxis] = useState<DataType>('netCalories');

  // Fetch all report entries
  const { data: reportEntries = [], isLoading, error } = useQuery({
    queryKey: ['report-entries'],
    queryFn: () => reportsApi.getEntries(),
  });

  // Filter data by date range
  const filteredData = useMemo(() => {
    if (!reportEntries.length) return [];

    let startDate: Date | null = null;
    const today = new Date();

    switch (dateRange) {
      case 'month':
        startDate = subMonths(today, 1);
        break;
      case 'year':
        startDate = subYears(today, 1);
        break;
      default:
        startDate = null;
    }

    const filtered = startDate
      ? reportEntries.filter((entry) => isAfter(parseISO(entry.date), startDate!))
      : reportEntries;

    // Sort by date ascending for chart
    return [...filtered].sort(
      (a, b) => parseISO(a.date).getTime() - parseISO(b.date).getTime()
    );
  }, [reportEntries, dateRange]);

  // Calculate 30-day moving averages
  const chartData = useMemo(() => {
    return filteredData.map((entry, index, arr) => {
      // Get last 30 entries for moving average
      const start = Math.max(0, index - 29);
      const window = arr.slice(start, index + 1);

      const weight30avg =
        window.reduce((sum, e) => sum + e.pounds, 0) / window.length;
      const calories30avg =
        window.reduce((sum, e) => sum + e.netCalories, 0) / window.length;

      return {
        ...entry,
        date: format(parseISO(entry.date), 'MMM d, yyyy'),
        dateObj: parseISO(entry.date),
        weight: entry.pounds,
        weight30avg: window.length >= 7 ? weight30avg : null,
        calories30avg: window.length >= 7 ? calories30avg : null,
      };
    });
  }, [filteredData]);

  // Calculate statistics
  const statistics = useMemo(() => {
    if (!chartData.length) {
      return null;
    }

    const weights = chartData.map((d) => d.weight).filter((w) => w > 0);
    const calories = chartData.map((d) => d.netCalories);

    const startWeight = weights[0] || 0;
    const endWeight = weights[weights.length - 1] || 0;
    const maxWeight = Math.max(...weights);
    const minWeight = Math.min(...weights);
    const avgWeight = weights.reduce((a, b) => a + b, 0) / weights.length;
    const weightChange = endWeight - startWeight;

    // Calculate days between first and last entry
    const firstDate = chartData[0]?.dateObj;
    const lastDate = chartData[chartData.length - 1]?.dateObj;
    const daysDiff = firstDate && lastDate
      ? Math.ceil((lastDate.getTime() - firstDate.getTime()) / (1000 * 60 * 60 * 24))
      : 0;
    const weeks = daysDiff / 7;
    const weightChangePerWeek = weeks > 0 ? weightChange / weeks : 0;

    const avgCalories =
      calories.reduce((a, b) => a + b, 0) / calories.length;

    return {
      startWeight,
      endWeight,
      maxWeight,
      minWeight,
      avgWeight,
      weightChange,
      weightChangePerWeek,
      weightRange: maxWeight - minWeight,
      avgCalories,
      totalDays: chartData.length,
    };
  }, [chartData]);

  // Date range options based on data availability
  const dateRangeOptions = useMemo(() => {
    const options: { value: DateRange; label: string }[] = [
      { value: 'all', label: 'All Dates' },
    ];

    if (reportEntries.length >= 30) {
      options.push({ value: 'month', label: 'Most Recent Month' });
    }
    if (reportEntries.length >= 365) {
      options.push({ value: 'year', label: 'Most Recent Year' });
    }

    return options;
  }, [reportEntries.length]);

  const handleDateRangeChange = (e: SelectChangeEvent<DateRange>) => {
    setDateRange(e.target.value as DateRange);
  };

  const handleLeftAxisChange = (e: SelectChangeEvent<DataType>) => {
    setLeftAxis(e.target.value as DataType);
  };

  const handleRightAxisChange = (e: SelectChangeEvent<DataType>) => {
    setRightAxis(e.target.value as DataType);
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error">Failed to load report data. Please try again later.</Alert>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Reports
      </Typography>

      {/* Controls */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} sm={4}>
              <FormControl fullWidth size="small">
                <InputLabel>Date Range</InputLabel>
                <Select
                  value={dateRange}
                  onChange={handleDateRangeChange}
                  label="Date Range"
                >
                  {dateRangeOptions.map((option) => (
                    <MenuItem key={option.value} value={option.value}>
                      {option.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={4}>
              <FormControl fullWidth size="small">
                <InputLabel>Left Axis</InputLabel>
                <Select
                  value={leftAxis}
                  onChange={handleLeftAxisChange}
                  label="Left Axis"
                >
                  {Object.entries(DATA_TYPE_LABELS).map(([value, label]) => (
                    <MenuItem key={value} value={value}>
                      {label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={4}>
              <FormControl fullWidth size="small">
                <InputLabel>Right Axis</InputLabel>
                <Select
                  value={rightAxis}
                  onChange={handleRightAxisChange}
                  label="Right Axis"
                >
                  {Object.entries(DATA_TYPE_LABELS).map(([value, label]) => (
                    <MenuItem key={value} value={value}>
                      {label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Chart */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          {chartData.length === 0 ? (
            <Alert severity="info">
              No data available for the selected date range.
            </Alert>
          ) : (
            <ResponsiveContainer width="100%" height={400}>
              <LineChart
                data={chartData}
                margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
              >
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis
                  dataKey="date"
                  tick={{ fontSize: 12 }}
                  interval="preserveStartEnd"
                />
                <YAxis
                  yAxisId="left"
                  orientation="left"
                  stroke={DATA_TYPE_COLORS[leftAxis]}
                  domain={['auto', 'auto']}
                  label={{
                    value: DATA_TYPE_LABELS[leftAxis],
                    angle: -90,
                    position: 'insideLeft',
                    style: { textAnchor: 'middle' },
                  }}
                />
                <YAxis
                  yAxisId="right"
                  orientation="right"
                  stroke={DATA_TYPE_COLORS[rightAxis]}
                  domain={['auto', 'auto']}
                  label={{
                    value: DATA_TYPE_LABELS[rightAxis],
                    angle: 90,
                    position: 'insideRight',
                    style: { textAnchor: 'middle' },
                  }}
                />
                <Tooltip />
                <Legend />
                <Line
                  yAxisId="left"
                  type="monotone"
                  dataKey={leftAxis}
                  stroke={DATA_TYPE_COLORS[leftAxis]}
                  name={DATA_TYPE_LABELS[leftAxis]}
                  dot={chartData.length < 100}
                  connectNulls
                />
                <Line
                  yAxisId="right"
                  type="monotone"
                  dataKey={rightAxis}
                  stroke={DATA_TYPE_COLORS[rightAxis]}
                  name={DATA_TYPE_LABELS[rightAxis]}
                  dot={chartData.length < 100}
                  connectNulls
                />
              </LineChart>
            </ResponsiveContainer>
          )}
        </CardContent>
      </Card>

      {/* Statistics */}
      {statistics && (
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Statistics
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Grid container spacing={3}>
              {/* Weight Stats */}
              <Grid item xs={12} md={6}>
                <Typography variant="subtitle1" sx={{ fontWeight: 'bold', mb: 1 }}>
                  Weight
                </Typography>
                <Grid container spacing={1}>
                  <Grid item xs={6}>
                    <StatItem label="Starting Weight" value={`${formatNumber(statistics.startWeight)} lbs`} />
                  </Grid>
                  <Grid item xs={6}>
                    <StatItem label="Current Weight" value={`${formatNumber(statistics.endWeight)} lbs`} />
                  </Grid>
                  <Grid item xs={6}>
                    <StatItem label="Max Weight" value={`${formatNumber(statistics.maxWeight)} lbs`} />
                  </Grid>
                  <Grid item xs={6}>
                    <StatItem label="Min Weight" value={`${formatNumber(statistics.minWeight)} lbs`} />
                  </Grid>
                  <Grid item xs={6}>
                    <StatItem
                      label="Weight Change"
                      value={`${statistics.weightChange >= 0 ? '+' : ''}${formatNumber(statistics.weightChange)} lbs`}
                      valueColor={statistics.weightChange < 0 ? 'success.main' : statistics.weightChange > 0 ? 'error.main' : undefined}
                    />
                  </Grid>
                  <Grid item xs={6}>
                    <StatItem
                      label="Change per Week"
                      value={`${statistics.weightChangePerWeek >= 0 ? '+' : ''}${formatNumber(statistics.weightChangePerWeek)} lbs`}
                      valueColor={statistics.weightChangePerWeek < 0 ? 'success.main' : statistics.weightChangePerWeek > 0 ? 'error.main' : undefined}
                    />
                  </Grid>
                  <Grid item xs={6}>
                    <StatItem label="Average Weight" value={`${formatNumber(statistics.avgWeight)} lbs`} />
                  </Grid>
                  <Grid item xs={6}>
                    <StatItem label="Weight Range" value={`${formatNumber(statistics.weightRange)} lbs`} />
                  </Grid>
                </Grid>
              </Grid>

              {/* Calorie Stats */}
              <Grid item xs={12} md={6}>
                <Typography variant="subtitle1" sx={{ fontWeight: 'bold', mb: 1 }}>
                  Calories
                </Typography>
                <Grid container spacing={1}>
                  <Grid item xs={6}>
                    <StatItem
                      label="Avg Net Calories/Day"
                      value={Math.round(statistics.avgCalories).toLocaleString()}
                    />
                  </Grid>
                  <Grid item xs={6}>
                    <StatItem label="Total Days Tracked" value={statistics.totalDays.toString()} />
                  </Grid>
                </Grid>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      )}
    </Box>
  );
}

interface StatItemProps {
  label: string;
  value: string;
  valueColor?: string;
}

function StatItem({ label, value, valueColor }: StatItemProps) {
  return (
    <Box sx={{ mb: 1 }}>
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
      <Typography
        variant="body1"
        sx={{ fontWeight: 'medium', color: valueColor || 'text.primary' }}
      >
        {value}
      </Typography>
    </Box>
  );
}
