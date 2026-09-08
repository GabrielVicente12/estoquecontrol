-- Execute conectado ao banco postgres como usuário postgres.
-- Cria somente bancos ausentes; não exclui nem limpa dados existentes.
SELECT 'CREATE DATABASE estoquecontrol_dev OWNER postgres'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'estoquecontrol_dev')\gexec
SELECT 'CREATE DATABASE estoquecontrol_test OWNER postgres'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'estoquecontrol_test')\gexec
