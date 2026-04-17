import {
  Entity,
  Column,
  PrimaryGeneratedColumn,
  ManyToOne,
  JoinColumn,
  CreateDateColumn,
  UpdateDateColumn,
} from 'typeorm';
import { ServingType } from '../common/enums/serving-type.enum';
import { User } from './user.entity';

@Entity('foods')
export class Food {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ManyToOne(() => User, (user) => user.foods, { nullable: true })
  @JoinColumn({ name: 'owner_id' })
  owner: User | null;

  @Column({ type: 'varchar', length: 50 })
  name: string;

  @Column({
    type: 'varchar',
    length: 10,
    name: 'default_serving_type',
  })
  defaultServingType: ServingType;

  @Column({ type: 'float', name: 'serving_type_qty' })
  servingTypeQty: number;

  @Column({ type: 'int' })
  calories: number;

  @Column({ type: 'float' })
  fat: number;

  @Column({ type: 'float', name: 'saturated_fat' })
  saturatedFat: number;

  @Column({ type: 'float' })
  carbs: number;

  @Column({ type: 'float' })
  fiber: number;

  @Column({ type: 'float' })
  sugar: number;

  @Column({ type: 'float' })
  protein: number;

  @Column({ type: 'float' })
  sodium: number;

  @CreateDateColumn({ name: 'created_time' })
  createdTime: Date;

  @UpdateDateColumn({ name: 'last_updated_time' })
  lastUpdatedTime: Date;
}
