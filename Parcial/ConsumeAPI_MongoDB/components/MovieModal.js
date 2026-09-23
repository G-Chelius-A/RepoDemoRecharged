import React from 'react';
import {
  Image,
  Modal,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';

export default function MovieModal({ movie, visible, onClose }) {
  if (!movie) {
    return null;
  }

  return (
    <Modal
      animationType="slide"
      transparent
      visible={visible}
      onRequestClose={onClose}
    >
      <View style={styles.overlay}>
        <View style={styles.modalCard}>
          <Pressable
            accessibilityLabel="Cerrar detalles de la película"
            accessibilityRole="button"
            onPress={onClose}
            style={styles.closeButton}
          >
            <Text style={styles.closeButtonText}>X</Text>
          </Pressable>

          <ScrollView contentContainerStyle={styles.content}>
            {movie.poster ? (
              <Image source={{ uri: movie.poster }} style={styles.poster} />
            ) : (
              <View style={[styles.poster, styles.noImageContainer]}>
                <Text style={styles.noImageText}>No Image</Text>
              </View>
            )}
            <Text style={styles.title}>{movie.title}</Text>
            <Text style={styles.sectionLabel}>Sinopsis</Text>
            <Text style={styles.plot}>
              {movie.fullplot || 'Sin descripción disponible.'}
            </Text>
          </ScrollView>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    justifyContent: 'flex-end',
    backgroundColor: 'rgba(15, 23, 42, 0.72)',
  },
  modalCard: {
    maxHeight: '88%',
    backgroundColor: '#FFF8ED',
    borderTopLeftRadius: 28,
    borderTopRightRadius: 28,
    overflow: 'hidden',
  },
  content: {
    alignItems: 'center',
    padding: 24,
    paddingTop: 36,
    paddingBottom: 34,
  },
  closeButton: {
    position: 'absolute',
    right: 18,
    top: 16,
    zIndex: 1,
    width: 38,
    height: 38,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 19,
    backgroundColor: '#F4C95D',
  },
  closeButtonText: {
    color: '#17213B',
    fontSize: 18,
    fontWeight: '800',
  },
  poster: {
    width: 190,
    height: 270,
    borderRadius: 16,
    backgroundColor: '#E7DED0',
  },
  noImageContainer: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  noImageText: {
    color: '#7A7063',
    fontSize: 14,
    fontWeight: '700',
  },
  title: {
    marginTop: 22,
    color: '#17213B',
    fontSize: 26,
    fontWeight: '800',
    textAlign: 'center',
  },
  sectionLabel: {
    alignSelf: 'flex-start',
    marginTop: 24,
    color: '#C84B31',
    fontSize: 13,
    fontWeight: '800',
    letterSpacing: 1,
    textTransform: 'uppercase',
  },
  plot: {
    alignSelf: 'flex-start',
    marginTop: 8,
    color: '#4D4A45',
    fontSize: 16,
    lineHeight: 25,
  },
});
