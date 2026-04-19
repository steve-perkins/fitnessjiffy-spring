import {
  Entity,
  Column,
  PrimaryGeneratedColumn,
  ManyToOne,
  JoinColumn,
  Unique,
} from 'typeorm';
import { User } from './user.entity';

@Entity('weights')
@Unique(['user', 'date'])
export class Weight {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ManyToOne(() => User, (user) => user.weights)
  @JoinColumn({ name: 'user_id' })
  user: User;

  @Column({
    type: 'date',
    transformer: {
      to: (value: Date | string): string => {
        if (!value) return value as string;
        // Extract YYYY-MM-DD from ISO string or Date object in UTC
        const date = value instanceof Date ? value : new Date(value);
        return date.toISOString().split('T')[0];
      },
      from: (value: string): Date => {
        if (!value) return value as any;
        // Parse as UTC date at midnight
        return new Date(value + 'T00:00:00.000Z');
      },
    },
  })
  date: Date;

  @Column({ type: 'float' })
  pounds: number;
}
