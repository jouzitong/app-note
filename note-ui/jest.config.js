module.exports = {
  testEnvironment: "node",
  roots: ["<rootDir>/src"],
  testMatch: ["**/__tests__/**/*.spec.js"],
  moduleNameMapper: {
    "^@/(.*)$": "<rootDir>/src/$1",
  },
  transform: {
    "^.+\\.js$": "babel-jest",
  },
  collectCoverageFrom: [
    "src/store/modules/**/*.js",
    "src/mappers/domain/language-jp/**/*.js",
    "!src/**/__tests__/**",
  ],
};
