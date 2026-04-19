import {
  Entity,
  Column,
  PrimaryGeneratedColumn,
  ManyToOne,
  JoinColumn,
  Unique,
} from 'typeorm';
import { User } from './user.entity';

@Entity('report_entries')
@Unique(['user', 'date'])
export class ReportEntry {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ManyToOne(() => User)
  @JoinColumn({ name: 'user_id' })
  user: User;

  @Column({
    type: 'date',
    transformer: {
      to: (value: Date | string): string => {
        if (!value) return value as string;
        const date = value instanceof Date ? value : new Date(value);
        return date.toISOString().split('T')[0];
      },
      from: (value: string): Date => {
        if (!value) return value as any;
        return new Date(value + 'T00:00:00.000Z');
      },
    },
  })
  date: Date;

  @Column({ type: 'float' })
  pounds: number;

  @Column({ type: 'int', name: 'net_calories' })
  netCalories: number;
}
