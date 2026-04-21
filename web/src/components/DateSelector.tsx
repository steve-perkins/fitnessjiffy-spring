import { TextField, Box } from '@mui/material';
import { format } from 'date-fns';
import { useDate } from '../context/DateContext';

export default function DateSelector() {
  const { selectedDate, setSelectedDate } = useDate();

  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newDate = new Date(e.target.value + 'T00:00:00');
    if (!isNaN(newDate.getTime())) {
      setSelectedDate(newDate);
    }
  };

  return (
    <Box sx={{ mb: 2 }}>
      <TextField
        type="date"
        value={format(selectedDate, 'yyyy-MM-dd')}
        onChange={handleDateChange}
        size="small"
        label="Date"
        InputLabelProps={{ shrink: true }}
      />
    </Box>
  );
}
