// Type models mirroring the SkillMirror REST API.

export interface ApiError {
  code: string;
  message: string;
  timestamp?: string;
  path?: string;
  fieldErrors?: { field: string; message: string }[];
}

// --- Auth ------------------------------------------------------------------

export interface UserSummary {
  id: number;
  name: string;
  email: string;
  role: 'USER' | 'ADMIN';
  profileCompleted: boolean;
  createdAt?: string;
}

export interface AuthResponse {
  token: string;
  user: UserSummary;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

// --- Profile ---------------------------------------------------------------

export interface Profile {
  id: number;
  userId: number;
  situation: string | null;
  domain: string | null;
  educationLevel: string | null;
  experienceSummary: string | null;
  targetJob: string | null;
  careerGoal: string | null;
  targetCompany: string | null;
  positionType: string | null;
  hoursPerWeek: number | null;
  learningPreference: string | null;
  preferredDifficulty: string | null;
  deadline: string | null;
  headline: string | null;
  bio: string | null;
  githubUrl: string | null;
  portfolioUrl: string | null;
  updatedAt: string;
}

export interface Experience {
  id: number;
  title: string;
  organization: string | null;
  type: string;
  description: string | null;
  startDate: string | null;
  endDate: string | null;
  current: boolean;
}

export interface Certificate {
  id: number;
  name: string;
  issuer: string | null;
  issuedDate: string | null;
  url: string | null;
}

export interface PortfolioProject {
  id: number;
  name: string;
  description: string | null;
  repositoryUrl: string | null;
  demoUrl: string | null;
  technologies: string | null;
  startedDate: string | null;
  endedDate: string | null;
}

// --- Skills ----------------------------------------------------------------

export type SkillLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT' | 'UNKNOWN';
export type SkillStatus = 'DECLARED' | 'EVALUATED' | 'DEMONSTRATED';
export type SkillCategory =
  | 'LANGUAGES' | 'WEB' | 'DATABASES' | 'DATA' | 'AI' | 'TOOLS' | 'SOFT_SKILLS' | 'OTHER';

export interface Skill {
  id: number;
  name: string;
  category: SkillCategory | string;
  custom: boolean;
}

export interface UserSkill {
  id: number;
  skillId: number;
  name: string;
  category: SkillCategory | string;
  level: SkillLevel;
  status: SkillStatus;
  notes: string | null;
  updatedAt: string;
}

// --- Questionnaire ---------------------------------------------------------

export interface QuestionnaireStep {
  id: string;
  index: number;
  title: string;
  description: string;
  fields: string[];
}

export interface QuestionnaireDefinition {
  signature: string;
  steps: QuestionnaireStep[];
}

export interface QuestionnaireSkill {
  name: string;
  skillId?: number;
  category?: string;
  level?: SkillLevel;
}

export interface QuestionnairePayload {
  situation?: {
    situation?: string;
    domain?: string;
    educationLevel?: string;
    experienceSummary?: string;
  };
  skills?: QuestionnaireSkill[];
  experience?: {
    projects?: { name: string; description?: string; repositoryUrl?: string; technologies?: string }[];
    experiences?: {
      title: string; organization?: string; type?: string; description?: string;
      startDate?: string; endDate?: string; current?: boolean;
    }[];
    certificates?: { name: string; issuer?: string; issuedDate?: string; url?: string }[];
    githubUrl?: string;
    portfolioUrl?: string;
  };
  objective?: {
    targetJob?: string;
    careerGoal?: string;
    targetCompany?: string;
    positionType?: string;
  };
  preferences?: {
    hoursPerWeek?: number;
    learningPreference?: string;
    preferredDifficulty?: string;
    deadline?: string;
  };
}

export interface QuestionnaireResponse {
  id: number | null;
  currentStep: number;
  completed: boolean;
  payload: QuestionnairePayload | null;
  updatedAt: string | null;
}

export interface CompleteResponse {
  profile: Profile;
  skills: number;
  experiences: number;
  projects: number;
  certificates: number;
  profileCompleted: boolean;
}

// --- Job offers ------------------------------------------------------------

export interface JobSkill {
  id: number;
  skillId: number | null;
  name: string;
  category: string;
  requirement: 'REQUIRED' | 'DESIRED';
  weight: number;
  requiredLevel: string | null;
  source: 'AI' | 'MANUAL';
  confirmed: boolean;
}

export interface JobOffer {
  id: number;
  title: string;
  company: string | null;
  description: string;
  experienceLevel: string | null;
  educationRequirement: string | null;
  extractionStatus: 'DRAFT' | 'EXTRACTED' | 'CONFIRMED';
  createdAt: string;
  updatedAt: string;
  skills: JobSkill[];
}

export interface JobSummary {
  id: number;
  title: string;
  company: string | null;
  extractionStatus: string;
  experienceLevel: string | null;
  skillCount: number;
  createdAt: string;
}

export interface ExtractResponse {
  job: JobOffer;
  simulated: boolean;
}

export interface ReviewedSkill {
  name: string;
  category?: string;
  requirement?: 'REQUIRED' | 'DESIRED';
  weight?: number;
  requiredLevel?: string;
}

// --- Analysis --------------------------------------------------------------

export type MatchCategory = 'DEMONSTRATED' | 'DECLARED' | 'PARTIAL' | 'MISSING';

export interface AnalysisSummary {
  id: number;
  jobOfferId: number;
  jobTitle: string;
  company: string | null;
  score: number;
  summary: string | null;
  simulatedAi: boolean;
  createdAt: string;
}

export interface AnalysisCounts {
  demonstrated: number;
  declared: number;
  partial: number;
  missing: number;
  total: number;
}

export interface AnalysisDetail {
  id: number;
  skillName: string;
  category: string;
  matchCategory: MatchCategory;
  requiredLevel: string | null;
  userLevel: SkillLevel | null;
  weight: number;
  masteryScore: number;
  priority: number;
  note: string | null;
  userSkillId: number | null;
}

export interface AnalysisResult {
  analysis: AnalysisSummary;
  explanation: string;
  counts: AnalysisCounts;
  details: AnalysisDetail[];
  simulatedAi: boolean;
}

// --- Learning --------------------------------------------------------------

export type PathStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'PAUSED' | 'COMPLETED' | 'VALIDATED';

export interface LearningTask {
  id: number;
  title: string;
  description: string | null;
  position: number;
  done: boolean;
  note: string | null;
  doneAt: string | null;
}

export interface Evidence {
  id: number;
  skillId: number | null;
  skillName: string;
  learningPathId: number | null;
  projectId: number | null;
  type: 'LINK' | 'FILE' | 'REPORT';
  url: string | null;
  fileName: string | null;
  description: string | null;
  status: 'SUBMITTED' | 'VERIFIED' | 'REJECTED';
  reviewNote: string | null;
  createdAt: string;
}

export interface LearningPath {
  id: number;
  analysisId: number | null;
  analysisDetailId: number | null;
  skillName: string;
  title: string;
  description: string | null;
  difficulty: string;
  objectives: string[];
  tools: string[];
  steps: string[];
  evaluationCriteria: string[];
  expectedResult: string | null;
  proofRequired: string | null;
  status: PathStatus;
  aiGenerated: boolean;
  simulatedAi: boolean;
  validationNote: string | null;
  createdAt: string;
  completedAt: string | null;
  taskCount: number;
  doneCount: number;
  progressPercent: number;
  tasks: LearningTask[];
  evidence: Evidence[];
}

export interface LearningPathSummary {
  id: number;
  skillName: string;
  title: string;
  difficulty: string;
  status: PathStatus;
  taskCount: number;
  doneCount: number;
  progressPercent: number;
  simulatedAi: boolean;
  createdAt: string;
  completedAt: string | null;
}

export interface CompletionCheck {
  canComplete: boolean;
  missing: string[];
  doneTasks: number;
  totalTasks: number;
  hasProof: boolean;
  criteriaDefined: boolean;
}

// --- Dashboard & progress --------------------------------------------------

export interface PrioritySkill {
  skillName: string;
  matchCategory: MatchCategory;
  priority: number;
  note: string | null;
}

export interface DashboardStats {
  activePaths: number;
  completedPaths: number;
  totalTasks: number;
  doneTasks: number;
  progressRate: number;
  totalSkills: number;
  demonstratedSkills: number;
  evaluatedSkills: number;
  declaredSkills: number;
  analysesCount: number;
  offersCount: number;
}

export interface RecommendedAction {
  title: string;
  description: string;
  url: string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
}

export interface Dashboard {
  firstName: string;
  profileCompleted: boolean;
  lastScore: number | null;
  lastScoreJob: string | null;
  lastAnalysisDate: string | null;
  prioritySkills: PrioritySkill[];
  stats: DashboardStats;
  recentAnalyses: AnalysisSummary[];
  recommendedActions: RecommendedAction[];
  emptyState: boolean;
}

export interface ScorePoint {
  analysisId: number;
  score: number;
  jobTitle: string;
  date: string;
}

export interface SkillProgress {
  userSkillId: number;
  skillName: string;
  category: string;
  level: SkillLevel;
  status: SkillStatus;
  updatedAt: string;
}

export interface TimelineEvent {
  type: 'ANALYSIS' | 'PATH' | 'EVIDENCE' | 'PROJECT';
  label: string;
  detail: string | null;
  date: string;
}

export interface ProgressReport {
  paths: LearningPathSummary[];
  scoreHistory: ScorePoint[];
  skillProgress: SkillProgress[];
  evidences: Evidence[];
  timeline: TimelineEvent[];
  totalTasks: number;
  doneTasks: number;
  verifiedProofs: number;
}

// --- Admin -----------------------------------------------------------------

export interface AdminUser {
  id: number;
  name: string;
  email: string;
  role: 'USER' | 'ADMIN';
  enabled: boolean;
  profileCompleted: boolean;
  createdAt: string;
  lastLoginAt: string | null;
  skillCount: number;
  analysisCount: number;
}

export interface AdminSkill {
  id: number;
  name: string;
  category: string;
  custom: boolean;
  createdBy: number | null;
}

export interface UsageItem {
  id: number;
  userId: number | null;
  purpose: string;
  mode: 'LIVE' | 'SIMULATED';
  model: string | null;
  promptTokens: number;
  completionTokens: number;
  latencyMs: number;
  status: 'SUCCESS' | 'ERROR';
  errorMessage: string | null;
  createdAt: string;
}

export interface UsageAgg {
  purpose: string;
  mode: string;
  calls: number;
  promptTokens: number;
  completionTokens: number;
}

export interface AiUsageReport {
  calls: number;
  simulatedCalls: number;
  liveCalls: number;
  errors: number;
  promptTokens: number;
  completionTokens: number;
  byPurpose: UsageAgg[];
  recent: UsageItem[];
}

export interface ErrorLogEntry {
  id: number;
  level: string;
  message: string;
  path: string | null;
  method: string | null;
  userId: number | null;
  createdAt: string;
}

export interface AppSetting {
  key: string;
  value: string;
  description: string | null;
}

export interface AdminOverview {
  users: number;
  enabledUsers: number;
  admins: number;
  skillCount: number;
  jobOffers: number;
  analyses: number;
  learningPaths: number;
  validatedPaths: number;
  evidences: number;
  aiCalls: number;
  errors: number;
  aiLiveMode: boolean;
}
