import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, Alert } from 'react-native';
import MemoryCard from '../components/MemoryCard';

const symbols = ['🍎','🍎','🚀','🚀','🐶','🐶','⭐','⭐','⚽','⚽','🎵','🎵'];

function shuffle(array) {
  return [...array].sort(() => Math.random() - 0.5);
}

export default function MemoryScreen() {
  const [cards, setCards] = useState([]);
  const [selected, setSelected] = useState([]);
  const [moves, setMoves] = useState(0);

  const newGame = () => {
    setCards(shuffle(symbols).map((value, id) => ({
      id, value, revealed: false, matched: false
    })));
    setSelected([]);
    setMoves(0);
  };

  useEffect(() => { newGame(); }, []);

  const selectCard = id => {
    if (selected.length === 2) return;

    const updated = cards.map(card =>
      card.id === id ? { ...card, revealed: true } : card
    );
    setCards(updated);

    const nextSelected = [...selected, id];
    setSelected(nextSelected);

    if (nextSelected.length === 2) {
      setMoves(m => m + 1);
      const [a, b] = nextSelected;
      const first = updated.find(c => c.id === a);
      const second = updated.find(c => c.id === b);

      if (first.value === second.value) {
        const matched = updated.map(c =>
          c.id === a || c.id === b ? { ...c, matched: true } : c
        );
        setTimeout(() => {
          setCards(matched);
          setSelected([]);
          if (matched.every(c => c.matched)) {
            Alert.alert('¡Ganaste!', `Terminaste en ${moves + 1} movimientos.`);
          }
        }, 300);
      } else {
        setTimeout(() => {
          setCards(current => current.map(c =>
            nextSelected.includes(c.id) ? { ...c, revealed: false } : c
          ));
          setSelected([]);
        }, 800);
      }
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>🧠 Memorama</Text>
      <Text style={styles.moves}>Movimientos: {moves}</Text>
      <View style={styles.grid}>
        {cards.map(card => (
          <MemoryCard
            key={card.id}
            value={card.value}
            revealed={card.revealed}
            matched={card.matched}
            onPress={() => selectCard(card.id)}
          />
        ))}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, alignItems: 'center', padding: 15, backgroundColor: '#f5f7fb' },
  title: { fontSize: 28, fontWeight: 'bold', color: '#182848' },
  moves: { margin: 8, fontSize: 17 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'center', maxWidth: 330 }
});
