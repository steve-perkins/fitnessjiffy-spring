import {
  Injectable,
  NotFoundException,
  ConflictException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, LessThanOrEqual } from 'typeorm';
import { Weight } from '../entities/weight.entity';
import { ReportEntriesService } from '../report-entries/report-entries.service';
import { CreateWeightDto } from './dto/create-weight.dto';
import { UpdateWeightDto } from './dto/update-weight.dto';

@Injectable()
export class WeightsService {
  constructor(
    @InjectRepository(Weight)
    private weightRepository: Repository<Weight>,
    private reportEntriesService: ReportEntriesService,
  ) {}

  async findAllByUser(userId: string): Promise<Weight[]> {
    return this.weightRepository.find({
      where: { user: { id: userId } },
      order: { date: 'DESC' },
    });
  }

  async findByUserAndDate(
    userId: string,
    date: Date,
  ): Promise<Weight | null> {
    return this.weightRepository.findOne({
      where: {
        user: { id: userId },
        date,
      },
    });
  }

  /**
   * Find the most recent weight entry on or before the specified date.
   * This implements gap-filling logic for days when weight wasn't recorded.
   */
  async findMostRecentOnDate(
    userId: string,
    date: Date,
  ): Promise<Weight | null> {
    return this.weightRepository.findOne({
      where: {
        user: { id: userId },
        date: LessThanOrEqual(date),
      },
      order: { date: 'DESC' },
    });
  }

  async create(
    userId: string,
    createWeightDto: CreateWeightDto,
  ): Promise<Weight> {
    const date = new Date(createWeightDto.date);

    // Check if weight already exists for this date
    const existing = await this.findByUserAndDate(userId, date);
    if (existing) {
      throw new ConflictException(
        `Weight entry already exists for date ${createWeightDto.date}`,
      );
    }

    const weight = this.weightRepository.create({
      user: { id: userId },
      date,
      pounds: createWeightDto.pounds,
    });

    const saved = await this.weightRepository.save(weight);

    // Update report entries from this date forward (weight affects calorie calculations)
    await this.reportEntriesService.updateFromDate(userId, date);

    return saved;
  }

  async update(
    id: string,
    userId: string,
    updateWeightDto: UpdateWeightDto,
  ): Promise<Weight> {
    const weight = await this.weightRepository.findOne({
      where: { id, user: { id: userId } },
    });

    if (!weight) {
      throw new NotFoundException(`Weight entry with ID ${id} not found`);
    }

    weight.pounds = updateWeightDto.pounds;
    const saved = await this.weightRepository.save(weight);

    // Update report entries from this date forward (weight affects calorie calculations)
    await this.reportEntriesService.updateFromDate(userId, weight.date);

    return saved;
  }

  async delete(id: string, userId: string): Promise<void> {
    const weight = await this.weightRepository.findOne({
      where: { id, user: { id: userId } },
    });

    if (!weight) {
      throw new NotFoundException(`Weight entry with ID ${id} not found`);
    }

    const date = weight.date;
    await this.weightRepository.remove(weight);

    // Update report entries from this date forward
    await this.reportEntriesService.updateFromDate(userId, date);
  }
}
