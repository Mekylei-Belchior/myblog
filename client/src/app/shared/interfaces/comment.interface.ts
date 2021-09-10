export interface CommentResponse {
  id: number;
  comment: string;
  date: string;
  author: string;
}

export type Comments = Array<CommentResponse>;
