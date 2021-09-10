export interface Post {
  id: string;
  title: string;
  author: string;
  date: string;
  content: string;
  tags: Array<string>;
}

export type ListOfNews = Array<Post>;
