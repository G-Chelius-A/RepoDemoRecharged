import React, { useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';

const AUTH_API_URL = 'https://turret-prepay-germinate.ngrok-free.dev';

export default function LoginScreen({ onLogin }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isRegistering, setIsRegistering] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit() {
    if (!email.trim() || !password) {
      setError('Ingresa tu correo y contraseña.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const endpoint = isRegistering ? '/register' : '/login';
      const response = await fetch(`${AUTH_API_URL}${endpoint}`, {
        method: 'POST',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
          'ngrok-skip-browser-warning': 'true',
        },
        body: JSON.stringify({ email: email.trim(), password }),
      });
      const responseText = await response.text();
      let data;

      try {
        data = JSON.parse(responseText);
      } catch {
        throw new Error('El servicio no devolvió JSON. Verifica que ngrok y el servidor estén activos.');
      }

      if (!response.ok) {
        throw new Error(data.error || 'No se pudo iniciar sesión.');
      }

      if (isRegistering) {
        setIsRegistering(false);
        setPassword('');
        setError('Usuario creado. Ahora puedes iniciar sesión.');
      } else {
        onLogin(data.user);
      }
    } catch (requestError) {
      setError(requestError.message || 'No se pudo conectar con el servicio de login.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <View style={styles.container}>
      <View style={styles.loginCard}>
        <Text style={styles.kicker}>SAMPLE MFLIX</Text>
        <Text style={styles.title}>{isRegistering ? 'Crear usuario' : 'Iniciar sesión'}</Text>
        <Text style={styles.subtitle}>
          {isRegistering ? 'Regístrate para acceder a la cartelera.' : 'Ingresa para ver la cartelera.'}
        </Text>

        <TextInput
          autoCapitalize="none"
          keyboardType="email-address"
          onChangeText={setEmail}
          placeholder="Correo electrónico"
          placeholderTextColor="#8D8A84"
          style={styles.input}
          value={email}
        />
        <TextInput
          autoCapitalize="none"
          onChangeText={setPassword}
          placeholder="Contraseña"
          placeholderTextColor="#8D8A84"
          secureTextEntry
          style={styles.input}
          value={password}
        />

        {error ? <Text style={styles.error}>{error}</Text> : null}

        <Pressable
          disabled={loading}
          onPress={handleSubmit}
          style={({ pressed }) => [styles.button, pressed && styles.buttonPressed]}
        >
          {loading ? (
            <ActivityIndicator color="#17213B" />
          ) : (
            <Text style={styles.buttonText}>{isRegistering ? 'Crear usuario' : 'Entrar'}</Text>
          )}
        </Pressable>

        <Pressable
          disabled={loading}
          onPress={() => {
            setIsRegistering(!isRegistering);
            setError('');
          }}
          style={styles.switchButton}
        >
          <Text style={styles.switchText}>
            {isRegistering ? 'Ya tengo una cuenta' : 'Crear una cuenta nueva'}
          </Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 24,
    backgroundColor: '#17213B',
  },
  loginCard: {
    width: '100%',
    maxWidth: 420,
    padding: 28,
    borderRadius: 22,
    backgroundColor: '#FFF8ED',
  },
  kicker: {
    color: '#C84B31',
    fontSize: 13,
    fontWeight: '800',
    letterSpacing: 1,
  },
  title: {
    marginTop: 10,
    color: '#17213B',
    fontSize: 32,
    fontWeight: '800',
  },
  subtitle: {
    marginTop: 8,
    marginBottom: 26,
    color: '#6A665F',
    fontSize: 15,
  },
  input: {
    height: 52,
    marginBottom: 14,
    paddingHorizontal: 16,
    borderWidth: 1,
    borderColor: '#DDD2C2',
    borderRadius: 12,
    color: '#17213B',
    backgroundColor: '#FFFFFF',
    fontSize: 16,
  },
  error: {
    marginBottom: 14,
    color: '#B4322A',
    fontSize: 14,
  },
  button: {
    alignItems: 'center',
    justifyContent: 'center',
    height: 52,
    borderRadius: 12,
    backgroundColor: '#F4C95D',
  },
  buttonPressed: {
    opacity: 0.75,
  },
  buttonText: {
    color: '#17213B',
    fontSize: 16,
    fontWeight: '800',
  },
  switchButton: {
    alignItems: 'center',
    marginTop: 18,
  },
  switchText: {
    color: '#C84B31',
    fontSize: 14,
    fontWeight: '700',
  },
});
