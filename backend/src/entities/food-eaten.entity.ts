import {
  Entity,
  Column,
  PrimaryGeneratedColumn,
  ManyToOne,
  JoinColumn,
  Unique,
} from 'typeorm';
import { User } from './user.entity';
import { Food } from './food.entity';
import { ServingType } from '../common/enums/serving-type.enum';

@Entity('foods_eaten')
@Unique(['user', 'food', 'date'])
export class FoodEaten {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ManyToOne(() => User, (user) => user.foodsEaten)
  @JoinColumn({ name: 'user_id' })
  user: User;

  @ManyToOne(() => Food, { eager: true })
  @JoinColumn({ name: 'food_id' })
  food: Food;

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

  @Column({
    type: 'varchar',
    length: 10,
    name: 'serving_type',
  })
  servingType: ServingType;

  @Column({ type: 'float', name: 'serving_qty' })
  servingQty: number;

  /**
   * CRITICAL: Complex serving size conversion logic
   * This method calculates the ratio between the serving size used
   * and the food's default serving size, taking into account
   * different serving type units (ounces, cups, tablespoons, etc.)
   *
   * Reference: FoodEaten.java:157-169
   */
  private getRatio(): number {
    let ratio: number;

    if (this.servingType === this.food.defaultServingType) {
      // Default serving type was used - simple division
      ratio = this.servingQty / this.food.servingTypeQty;
    } else {
      // Serving type needs conversion through ounces
      // Each ServingType enum value represents its ounce equivalent
      const ouncesInThisServingType = this.servingType as number; // enum numeric value
      const ouncesInDefaultServingType = this.food
        .defaultServingType as number; // enum numeric value
      const denominator =
        ouncesInDefaultServingType * this.food.servingTypeQty;

      ratio =
        denominator === 0
          ? 0
          : (ouncesInThisServingType * this.servingQty) / denominator;
    }

    return ratio;
  }

  /**
   * Calculate calories for this food eaten instance
   * Uses Math.floor to match Java's int cast behavior
   */
  getCalories(): number {
    return Math.floor(this.food.calories * this.getRatio());
  }

  getFat(): number {
    return this.food.fat * this.getRatio();
  }

  getSaturatedFat(): number {
    return this.food.saturatedFat * this.getRatio();
  }

  getSodium(): number {
    return this.food.sodium * this.getRatio();
  }

  getCarbs(): number {
    return this.food.carbs * this.getRatio();
  }

  getFiber(): number {
    return this.food.fiber * this.getRatio();
  }

  getSugar(): number {
    return this.food.sugar * this.getRatio();
  }

  getProtein(): number {
    return this.food.protein * this.getRatio();
  }
}
