module.exports = {
  projects: [
    {
      displayName: 'unit',
      testMatch: ['<rootDir>/src/**/*.spec.ts'],
      testEnvironment: 'node',
      preset: 'ts-jest',
      collectCoverageFrom: [
        'src/**/*.ts',
        '!src/**/*.interface.ts',
        '!src/**/*.dto.ts',
        '!src/**/*.entity.ts',
        '!src/**/*.module.ts',
        '!src/main.ts',
      ],
    },
    {
      displayName: 'integration',
      testMatch: ['<rootDir>/test/integration/**/*.int.ts'],
      testEnvironment: 'node',
      preset: 'ts-jest',
      timeout: 30000,
    },
    {
      displayName: 'e2e',
      testMatch: ['<rootDir>/test/e2e/**/*.e2e-spec.ts'],
      testEnvironment: 'node',
      preset: 'ts-jest',
      timeout: 60000,
    },
  ],
};
