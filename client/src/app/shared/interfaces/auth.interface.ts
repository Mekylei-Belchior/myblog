export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface Token {
  accessToken: string;
  refreshToken: string;
}
