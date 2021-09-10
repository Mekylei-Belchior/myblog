import { Comments } from './comment.interface';

export interface FullNews {
  id: number;
  title: string;
  author: string;
  date: string;
  content: string;
  comment: Comments;
  tag: Array<string>;
}
