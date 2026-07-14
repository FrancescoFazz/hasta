export interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;    //secondi di validità dell'access_token
  refresh_expires_in: number;
  token_type: string;
}

export interface DecodedToken {
  sub: string;
  preferred_username: string;
  email?: string;
  exp: number;    //timestamp di scadenza
  [key: string]: unknown;
}
