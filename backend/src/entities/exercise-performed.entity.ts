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

  @Column({ type: 'int' })
  minutes: number;

  /**
   * Calculate calories burned for this exercise
   * Formula: MET × 3.5 × weight_kg ÷ 200 × minutes
   *
   * Reference: ExerciseService.java:175-182
   *
   * @param weightInPounds - User's weight on the date of exercise
   * @returns Calories burned (as integer, matching Java's int cast)
   */
  getCaloriesBurned(weightInPounds: number): number {
    const weightInKilograms = weightInPounds / 2.2;
    return Math.floor(
      (this.exercise.metabolicEquivalent * 3.5 * weightInKilograms * this.minutes) / 200,
    );
  }
}
