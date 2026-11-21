-- ==============================================================================
-- CHELIUS CHESS - ESQUEMA DE BASE DE DATOS SUPABASE (MULTIJUGADOR)
-- Copia y ejecuta este script en el SQL Editor de tu consola Supabase.
-- ==============================================================================

-- 1. Habilitar extensión UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. Tabla de Perfiles de Usuario (Vinculada a auth.users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT NOT NULL,
    avatar_url TEXT,
    rating INT DEFAULT 1200 NOT NULL,
    wins INT DEFAULT 0 NOT NULL,
    losses INT DEFAULT 0 NOT NULL,
    draws INT DEFAULT 0 NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

-- 3. Trigger para crear Perfil Automático cuando un usuario se registra en Supabase Auth
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, username)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'username', split_part(NEW.email, '@', 1))
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- 4. Tabla de Partidas en Vivo (Games)
CREATE TABLE IF NOT EXISTS public.games (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    white_player_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    black_player_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    status TEXT DEFAULT 'WAITING' NOT NULL, -- 'WAITING', 'IN_PROGRESS', 'FINISHED'
    turn TEXT DEFAULT 'WHITE' NOT NULL,     -- 'WHITE', 'BLACK'
    current_fen TEXT DEFAULT 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1' NOT NULL,
    last_move_san TEXT,
    time_config_minutes INT DEFAULT 10 NOT NULL,
    time_config_increment INT DEFAULT 0 NOT NULL,
    white_time_left_ms BIGINT DEFAULT 600000 NOT NULL,
    black_time_left_ms BIGINT DEFAULT 600000 NOT NULL,
    last_move_timestamp TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    winner_id UUID REFERENCES public.profiles(id),
    end_reason TEXT,
    score TEXT DEFAULT '*' NOT NULL,        -- '*', '1-0', '0-1', '1/2-1/2'
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

-- 5. Habilitar Seguridad de Nivel de Fila (RLS)
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.games ENABLE ROW LEVEL SECURITY;

-- 6. Políticas RLS para PROFILES
DROP POLICY IF EXISTS "Public Profiles Read" ON public.profiles;
CREATE POLICY "Public Profiles Read"
    ON public.profiles FOR SELECT
    USING (true);

DROP POLICY IF EXISTS "User Update Own Profile" ON public.profiles;
CREATE POLICY "User Update Own Profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id);

DROP POLICY IF EXISTS "User Insert Own Profile" ON public.profiles;
CREATE POLICY "User Insert Own Profile"
    ON public.profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

-- 7. Políticas RLS para GAMES
DROP POLICY IF EXISTS "Games Read Access" ON public.games;
CREATE POLICY "Games Read Access"
    ON public.games FOR SELECT
    USING (true);

DROP POLICY IF EXISTS "Create Game Policy" ON public.games;
CREATE POLICY "Create Game Policy"
    ON public.games FOR INSERT
    WITH CHECK (auth.uid() = white_player_id);

DROP POLICY IF EXISTS "Update Game Policy" ON public.games;
CREATE POLICY "Update Game Policy"
    ON public.games FOR UPDATE
    USING (
        auth.uid() = white_player_id OR
        auth.uid() = black_player_id OR
        black_player_id IS NULL
    );

-- 8. HABILITAR SUPABASE REALTIME EN LA TABLA games (Sincronización WebSockets en Vivo)
-- Es indispensable ejecutar esta instrucción para recibir las jugadas en tiempo real sin latencia.
ALTER PUBLICATION supabase_realtime ADD TABLE public.games;
