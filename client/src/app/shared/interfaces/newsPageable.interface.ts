import { ListOfNews } from './ResponseNews.interface';

export interface NewsPageable {
  _embedded: Embedded;
  _links: Links;
  page: Page;
}

interface Embedded {
  newsList: ListOfNews;
}

interface Links {
  self: {
      href: string;
  };
}

interface Page {
  size: number;
  totalElements: number;
  totalPages: number;
  number: number;
}