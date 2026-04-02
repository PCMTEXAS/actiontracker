export type UserRole = 'OWNER' | 'MEMBER';

export interface AppUser {
  id: string;
  email: string;
  name: string;
  pictureUrl: string | null;
  role: UserRole;
  dailyDigestEnabled: boolean;
}
