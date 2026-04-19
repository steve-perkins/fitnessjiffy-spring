import {
  Entity,
  Column,
  PrimaryGeneratedColumn,
  OneToMany,
  CreateDateColumn,
  UpdateDateColumn,
} from 'typeorm';
import { Sex } from '../common/enums/sex.enum';
import { ActivityLevel } from '../common/enums/activity-level.enum';

@Entity('users')
export class User {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ type: 'varchar', length: 10 })
  sex: Sex;

  @Column({
    type: 'date',
    name: 'birthdate',
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
  birthdate: Date;

  @Column({ type: 'float', name: 'height_in_inches' })
  heightInInches: number;

  @Column({
    type: 'float',
    name: 'activity_level',
    transformer: {
      // Convert from ActivityLevel enum (number) to database value (float)
      to: (value: ActivityLevel) => (value !== undefined ? (value as number) : null),
      // Convert from database value (float) to ActivityLevel enum
      from: (value: number) => (value !== null && value !== undefined ? (value as ActivityLevel) : null),
    },
  })
  activityLevel: ActivityLevel;

  @Column({ type: 'varchar', length: 100, unique: true })
  email: string;

  @Column({
    type: 'varchar',
    length: 100,
    nullable: true,
    name: 'password_hash',
  })
  passwordHash: string | null;

  @Column({ type: 'varchar', length: 20, name: 'first_name' })
  firstName: string;

  @Column({ type: 'varchar', length: 20, name: 'last_name' })
  lastName: string;

  @Column({ type: 'varchar', length: 50, name: 'timezone' })
  timezone: string;

  @CreateDateColumn({ name: 'created_time' })
  createdTime: Date;

  @UpdateDateColumn({ name: 'last_updated_time' })
  lastUpdatedTime: Date;

  // Relationships - Note: Using lazy loading pattern
  // These will be defined with imports to avoid circular dependencies
  @OneToMany('Weight', 'user')
  weights: Promise<any[]>;

  @OneToMany('Food', 'owner')
  foods: Promise<any[]>;

  @OneToMany('FoodEaten', 'user')
  foodsEaten: Promise<any[]>;

  @OneToMany('ExercisePerformed', 'user')
  exercisesPerformed: Promise<any[]>;
}
