import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView } from 'react-native';
import Logo from '../components/Logo';

export default function HomeScreen({ navigation }) {
  const go = screen => navigation.navigate(screen);

  const items = [
    { name: 'Dados', icon: '🎲', description: 'Lanza uno o dos dados.', screen: 'Dados' },
    { name: 'Memorama', icon: '🧠', description: 'Encuentra todas las parejas.', screen: 'Memorama' },
    { name: 'Divisas', icon: '💱', description: 'Convierte monedas con tasas editables.', screen: 'Divisas' },
    { name: 'IMC', icon: '⚖️', description: 'Calcula tu índice de masa corporal.', screen: 'IMC' }
  ];

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.header}>
        <Logo size="large" />
        <Text style={styles.welcome}>¡Bienvenido a Vida Station!</Text>
        <Text style={styles.subtitle}>
          Una estación de herramientas para jugar, aprender y cuidar tu bienestar.
        </Text>
      </View>

      {items.map(item => (
        <TouchableOpacity
          key={item.name}
          style={styles.card}
          activeOpacity={0.85}
          onPress={() => go(item.screen)}
        >
          <View style={styles.cardHeader}>
            <Text style={styles.emoji}>{item.icon}</Text>
            <View style={styles.cardTextWrap}>
              <Text style={styles.cardTitle}>{item.name}</Text>
              <Text style={styles.cardText}>{item.description}</Text>
            </View>
          </View>
        </TouchableOpacity>
      ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 24,
    paddingBottom: 40,
    alignItems: 'center',
    backgroundColor: '#f4f7ff'
  },
  header: {
    width: '100%',
    alignItems: 'center',
    marginBottom: 10
  },
  welcome: {
    fontSize: 25,
    fontWeight: 'bold',
    textAlign: 'center',
    color: '#182848'
  },
  subtitle: {
    textAlign: 'center',
    color: '#5a6477',
    marginVertical: 12,
    lineHeight: 21,
    maxWidth: 320
  },
  card: {
    width: '100%',
    padding: 18,
    marginVertical: 8,
    borderRadius: 18,
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#dfe8ff',
    shadowColor: '#182848',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.08,
    shadowRadius: 10,
    elevation: 4
  },
  cardHeader: {
    flexDirection: 'row',
    alignItems: 'center'
  },
  emoji: {
    fontSize: 34,
    marginRight: 15
  },
  cardTextWrap: {
    flex: 1
  },
  cardTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#182848'
  },
  cardText: {
    color: '#5a6477',
    marginTop: 4
  }
});
