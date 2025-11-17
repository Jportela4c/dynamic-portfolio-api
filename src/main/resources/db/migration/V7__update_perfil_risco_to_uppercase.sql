-- Update perfil_adequado values to UPPERCASE to match Java enum convention
UPDATE produtos SET perfil_adequado = 'CONSERVADOR' WHERE perfil_adequado = 'Conservador';
UPDATE produtos SET perfil_adequado = 'MODERADO' WHERE perfil_adequado = 'Moderado';
UPDATE produtos SET perfil_adequado = 'AGRESSIVO' WHERE perfil_adequado = 'Agressivo';
