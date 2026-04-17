import { ListOfNews } from './ResponseNews.interface';

export interface NewsPageable {
  content: ListOfNews;
  page: Page;
}

interface Page {
  size: number;
  totalElements: number;
  totalPages: number;
  number: number;
}
