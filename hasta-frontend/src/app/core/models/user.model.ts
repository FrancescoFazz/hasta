export interface User {
  id: number;
  username: string;
  email: string;
  name: string;
  surname: string;
  role: string;
  balance: number;
  gender: string;
  createdAt: string;
  updatedAt: string;
}

export type Gender = 'MALE' | 'FEMALE';

export interface CreateUserRequest {
  username: string;
  password: string;
  email: string;
  name: string;
  surname: string;
  gender: Gender;
}
