import { ListOfNews } from './ResponseNews.interface';

export interface NewsPageable {
  content: ListOfNews;
  pageable: object[];
  last: boolean;
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  sort: boolean;
  first: boolean;
  numberOfElements: number;
}
