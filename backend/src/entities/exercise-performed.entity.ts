import {
  Entity,
  Column,
  PrimaryGeneratedColumn,
  ManyToOne,
  JoinColumn,
  Unique,
} from 'typeorm';
import { User } from './user.entity';
import { Exercise } from './exercise.entity';

@Entity('exercises_performed')
@Unique(['user', 'exercise', 'date'])
export class ExercisePerformed {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ManyToOne(() => User, (user) => user.exercisesPerformed)
  @JoinColumn({ name: 'user_id' })
  user: User;

  @ManyToOne(() => Exercise, { eager: true })
  @JoinColumn({ name: 'exercise_id' })
  exercise: Exercise;

  @Column({ type: 'date' })
  date: Date;

  @Column({ type: 'int' })
  minutes: number;

  /**
   * Calculate calories burned for this exercise
   * Formula: MET × 3.5 × weight_kg ÷ 200 × minutes
   * NOTE: This requires the user's weight on the given date,
   * so it should be implemented in the service layer, not here
   */
}
