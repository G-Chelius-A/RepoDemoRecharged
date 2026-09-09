import React from 'react';
import { TouchableOpacity, Text, StyleSheet } from 'react-native';

export default function MemoryCard({ value, revealed, matched, onPress }) {
  return (
    <TouchableOpacity
      disabled={revealed || matched}
      onPress={onPress}
      style={[
        styles.card,
        revealed && styles.revealed,
        matched && styles.matched
      ]}
    >
      <Text style={styles.text}>{revealed || matched ? value : '?'}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    width: 72, height: 72, margin: 5, borderRadius: 12,
    backgroundColor: '#182848', alignItems: 'center', justifyContent: 'center'
  },
  revealed: { backgroundColor: '#4b6cb7' },
  matched: { backgroundColor: '#4caf50' },
  text: { color: '#fff', fontSize: 28, fontWeight: 'bold' }
});
