import { createContext, useContext, useState, ReactNode } from 'react';
import { format, parseISO } from 'date-fns';

interface DateContextType {
  selectedDate: Date;
  setSelectedDate: (date: Date) => void;
  dateString: string; // ISO format for API calls
  displayDate: string; // Human readable format
}

const DateContext = createContext<DateContextType | undefined>(undefined);

interface DateProviderProps {
  children: ReactNode;
}

export function DateProvider({ children }: DateProviderProps) {
  const [selectedDate, setSelectedDate] = useState<Date>(() => {
    // Try to restore from URL or default to today
    const urlParams = new URLSearchParams(window.location.search);
    const dateParam = urlParams.get('date');
    if (dateParam) {
      try {
        return parseISO(dateParam);
      } catch {
        return new Date();
      }
    }
    return new Date();
  });

  const dateString = format(selectedDate, "yyyy-MM-dd'T'00:00:00.000'Z'");
  const displayDate = format(selectedDate, 'MMMM d, yyyy');

  const value: DateContextType = {
    selectedDate,
    setSelectedDate,
    dateString,
    displayDate,
  };

  return <DateContext.Provider value={value}>{children}</DateContext.Provider>;
}

export function useDate() {
  const context = useContext(DateContext);
  if (context === undefined) {
    throw new Error('useDate must be used within a DateProvider');
  }
  return context;
}
