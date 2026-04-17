import {
  Entity,
  Column,
  PrimaryGeneratedColumn,
  CreateDateColumn,
  UpdateDateColumn,
} from 'typeorm';

@Entity('exercises')
export class Exercise {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ type: 'varchar', length: 5 })
  code: string;

  @Column({ type: 'float', name: 'metabolic_equivalent' })
  metabolicEquivalent: number;

  @Column({ type: 'varchar', length: 25 })
  category: string;

  @Column({ type: 'varchar', length: 250 })
  description: string;

  @CreateDateColumn({ name: 'created_time' })
  createdTime: Date;

  @UpdateDateColumn({ name: 'last_updated_time' })
  lastUpdatedTime: Date;
}
