-- Desabilitar temporariamente a integridade referencial para inserção de dados
SET REFERENTIAL_INTEGRITY FALSE;

CREATE SCHEMA IF NOT EXISTS PUBLIC;

-- Cria a tabela de usuários
CREATE TABLE PUBLIC.users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255) NOT NULL,
    created TIMESTAMP DEFAULT NOW(),
    active BOOLEAN DEFAULT TRUE
);

-- Inserir registros na tabela users
INSERT INTO users (name, email, password, created, active) VALUES
('Administrador', 'adm@email.com', '$2a$12$2S9VpKjZWuJO04l9zN264ur3kd.BwnJQWPIhQwx8h0GSSX/JJewZe', NOW(), TRUE),
('Dr. Carlos Silva', 'carlos.silva@email.com', '$2a$12$6WCRXl.w1m4DI8VxWtf6I.O1RR1bjkKBtjLDPHTHM20EUV6UXdbUa', NOW(), TRUE),
('Dra. Ana Souza', 'ana.souza@email.com', '$2a$12$6WCRXl.w1m4DI8VxWtf6I.O1RR1bjkKBtjLDPHTHM20EUV6UXdbUa', NOW(), TRUE),
('João Pereira', 'joao.pereira@email.com', '$2a$12$6WCRXl.w1m4DI8VxWtf6I.O1RR1bjkKBtjLDPHTHM20EUV6UXdbUa', NOW(), TRUE),
('Maria Fernandes', 'maria.fernandes@email.com', '$2a$12$6WCRXl.w1m4DI8VxWtf6I.O1RR1bjkKBtjLDPHTHM20EUV6UXdbUa', NOW(), TRUE),
('Pedro Almeida', 'pedro.almeida@email.com', '$2a$12$6WCRXl.w1m4DI8VxWtf6I.O1RR1bjkKBtjLDPHTHM20EUV6UXdbUa', NOW(), TRUE),
('Carla Mendes', 'carla.mendes@email.com', '$2a$12$6WCRXl.w1m4DI8VxWtf6I.O1RR1bjkKBtjLDPHTHM20EUV6UXdbUa', NOW(), TRUE),
('Lucas Oliveira', 'lucas.oliveira@email.com', '$2a$12$6WCRXl.w1m4DI8VxWtf6I.O1RR1bjkKBtjLDPHTHM20EUV6UXdbUa', NOW(), TRUE),
('Dr. Ricardo Santos', 'ricardo.santos@email.com', '$2a$12$6WCRXl.w1m4DI8VxWtf6I.O1RR1bjkKBtjLDPHTHM20EUV6UXdbUa', NOW(), TRUE),
('Fernanda Costa', 'fernanda.costa@email.com', '$2a$12$6WCRXl.w1m4DI8VxWtf6I.O1RR1bjkKBtjLDPHTHM20EUV6UXdbUa', NOW(), TRUE),
('Patrícia Lima', 'patricia.lima@email.com', '$2a$12$6WCRXl.w1m4DI8VxWtf6I.O1RR1bjkKBtjLDPHTHM20EUV6UXdbUa', NOW(), TRUE);

-- Criar tabela roles
CREATE TABLE PUBLIC.roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Inserir registros na tabela roles
INSERT INTO PUBLIC.roles (name) VALUES
('ROLE_ADMIN'),
('ROLE_USER');

-- Criar tabela de User x Roles
CREATE TABLE PUBLIC.user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES PUBLIC.users(id),
    FOREIGN KEY (role_id) REFERENCES PUBLIC.roles(id)
);

-- Associar usuários a roles
INSERT INTO PUBLIC.user_roles (user_id, role_id) VALUES
-- Administrador é um ADMIN e um USER
(1, 1), -- user_id = 1 (Dr. Carlos Silva), role_id = 1 (ROLE_ADMIN)
(1, 2), -- user_id = 1 (Dr. Carlos Silva), role_id = 2 (ROLE_USER)

-- Dra. Ana Souza é um USER
(2, 2), -- user_id = 2 (Dra. Ana Souza), role_id = 2 (ROLE_USER)

-- João Pereira é um USER
(3, 2), -- user_id = 3 (João Pereira), role_id = 2 (ROLE_USER)

-- Maria Fernandes é um USER
(4, 2), -- user_id = 4 (Maria Fernandes), role_id = 2 (ROLE_USER)

-- Pedro Almeida é um USER
(5, 2), -- user_id = 5 (Pedro Almeida), role_id = 2 (ROLE_USER)

-- Carla Mendes é um USER
(6, 2), -- user_id = 6 (Carla Mendes), role_id = 2 (ROLE_USER)

-- Lucas Oliveira é um USER
(7, 2), -- user_id = 7 (Lucas Oliveira), role_id = 2 (ROLE_USER)

-- Dr. Ricardo Santos é um USER
(8, 2), -- user_id = 8 (Dr. Ricardo Santos), role_id = 2 (ROLE_USER)

-- Fernanda Costa é um USER
(9, 2), -- user_id = 9 (Fernanda Costa), role_id = 2 (ROLE_USER)

-- Patrícia Lima é um USER
(10, 2), -- user_id = 10 (Patrícia Lima), role_id = 2 (ROLE_USER)

-- Dr. Carlos Silva um USER
(11, 2); -- user_id = 1 (Dr. Carlos Silva), role_id = 2 (ROLE_USER)

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