-- Desabilitar temporariamente a integridade referencial para inserção de dados
SET REFERENTIAL_INTEGRITY FALSE;

CREATE SCHEMA IF NOT EXISTS PUBLIC;

-- Criar tabela News
CREATE TABLE PUBLIC.news (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    date TIMESTAMP NOT NULL,
    content CLOB
);

-- Criar tabela Comments
CREATE TABLE PUBLIC.comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    comment CLOB,
    date TIMESTAMP NOT NULL,
    author VARCHAR(100),
    news_id BIGINT,
    FOREIGN KEY (news_id) REFERENCES news(id)
);

-- Criar tabela news_tags
CREATE TABLE news_tags (
    id BIGINT NOT NULL,
    news_tags VARCHAR(255),
    FOREIGN KEY (id) REFERENCES news(id)
);

-- Inserir registros na tabela News
INSERT INTO news (title, author, date, content) VALUES
('Inteligência Artificial transforma a medicina', 'Dr. Carlos Silva', NOW(), 'A Inteligência Artificial está revolucionando a medicina, com algoritmos capazes de diagnosticar doenças com precisão e agilidade.'),
('Novo planeta habitável descoberto', 'Dra. Ana Souza', NOW(), 'Cientistas descobriram um novo planeta na zona habitável de uma estrela próxima, aumentando as esperanças de vida fora da Terra.'),
('Copa do Mundo 2026: Sedes anunciadas', 'João Pereira', NOW(), 'As cidades-sede da Copa do Mundo de 2026 foram anunciadas, com jogos espalhados por três países.'),
('Exposição de arte moderna atrai milhares', 'Maria Fernandes', NOW(), 'Uma exposição de arte moderna no Museu Nacional atraiu milhares de visitantes no primeiro fim de semana.'),
('Blockchain e o futuro das transações financeiras', 'Pedro Almeida', NOW(), 'A tecnologia blockchain promete revolucionar as transações financeiras, oferecendo segurança e transparência.'),
('Mudanças climáticas e o futuro do planeta', 'Carla Mendes', NOW(), 'As mudanças climáticas são uma ameaça real, e especialistas discutem soluções para mitigar seus efeitos.'),
('Novo filme da Marvel bate recordes de bilheteria', 'Lucas Oliveira', NOW(), 'O mais recente filme da Marvel, "Vingadores: Ultimato", já é o maior sucesso de bilheteria da história.'),
('Descoberta de nova espécie de dinossauro', 'Dr. Ricardo Santos', NOW(), 'Paleontólogos descobriram uma nova espécie de dinossauro que viveu há mais de 70 milhões de anos.'),
('Tendências de tecnologia para 2025', 'Fernanda Costa', NOW(), 'Especialistas listam as principais tendências tecnológicas que devem dominar o mercado em 2025.'),
('A importância da leitura na infância', 'Patrícia Lima', NOW(), 'Estudos mostram que o hábito da leitura na infância é fundamental para o desenvolvimento cognitivo e emocional.');

-- Inserir registros na tabela Comments
INSERT INTO comments (comment, date, author, news_id) VALUES
('A IA realmente está mudando a medicina!', NOW(), 'Carlos Mendes', 1),
('Quais são as limitações da IA na medicina?', NOW(), 'Ana Paula', 1),
('Esse novo planeta pode ser nossa nova casa!', NOW(), 'Roberto Alves', 2),
('Como será a vida nesse novo planeta?', NOW(), 'Mariana Costa', 2),
('Mal posso esperar pela Copa de 2026!', NOW(), 'João Pedro', 3),
('Quais são as cidades-sede?', NOW(), 'Fernanda Silva', 3),
('A exposição foi incrível!', NOW(), 'Lucas Souza', 4),
('Qual foi a obra mais impressionante?', NOW(), 'Carla Oliveira', 4),
('Blockchain é o futuro!', NOW(), 'Pedro Henrique', 5),
('Quais são os desafios da blockchain?', NOW(), 'Ana Clara', 5),
('Precisamos agir agora contra as mudanças climáticas!', NOW(), 'Roberto Santos', 6),
('Quais são as soluções propostas?', NOW(), 'Mariana Lima', 6),
('O filme foi incrível!', NOW(), 'Lucas Mendes', 7),
('Qual foi a cena mais emocionante?', NOW(), 'Carla Souza', 7),
('Essa descoberta é fascinante!', NOW(), 'Ricardo Almeida', 8),
('Onde foi encontrado o fóssil?', NOW(), 'Patrícia Oliveira', 8),
('As tendências são promissoras!', NOW(), 'Fernando Costa', 9),
('Quais são as tecnologias mais impactantes?', NOW(), 'Ana Paula', 9),
('A leitura é essencial para as crianças!', NOW(), 'Roberto Lima', 10),
('Como incentivar a leitura?', NOW(), 'Mariana Silva', 10);

-- Inserir registros na tabela news_tags
INSERT INTO news_tags (id, news_tags) VALUES
(1, 'Inteligência Artificial'),
(1, 'Medicina'),
(1, 'Tecnologia'),
(2, 'Astronomia'),
(2, 'Planeta Habitável'),
(2, 'Ciência'),
(3, 'Copa do Mundo'),
(3, 'Esportes'),
(3, '2026'),
(4, 'Arte Moderna'),
(4, 'Cultura'),
(4, 'Exposição'),
(5, 'Blockchain'),
(5, 'Tecnologia'),
(5, 'Finanças'),
(6, 'Mudanças Climáticas'),
(6, 'Meio Ambiente'),
(6, 'Sustentabilidade'),
(7, 'Marvel'),
(7, 'Cinema'),
(7, 'Bilheteria'),
(8, 'Dinossauro'),
(8, 'Paleontologia'),
(8, 'Ciência'),
(9, 'Tecnologia'),
(9, 'Tendências'),
(9, '2025'),
(10, 'Leitura'),
(10, 'Educação'),
(10, 'Infância');

-- Habilitar novamente a integridade referencial
SET REFERENTIAL_INTEGRITY TRUE;