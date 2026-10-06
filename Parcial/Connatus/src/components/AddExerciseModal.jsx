import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  View,
  Text,
  Modal,
  TextInput,
  TouchableOpacity,
  ScrollView,
  Image,
  Alert,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import { MUSCLES, getTipoFromMusculo } from '../services/storageService';
import { COLORS, FONTS, GLOBAL_STYLES } from '../theme/theme';

export default function AddExerciseModal({ visible, onClose, exerciseToEdit, onSave }) {
  const [nombre, setNombre] = useState('');
  const [musculo, setMusculo] = useState(MUSCLES[0]);
  const [imagenUri, setImagenUri] = useState(null);

  const isEditing = !!exerciseToEdit;

  useEffect(() => {
    if (exerciseToEdit) {
      setNombre(exerciseToEdit.nombre || '');
      setMusculo(exerciseToEdit.musculo || MUSCLES[0]);
      setImagenUri(exerciseToEdit.imagen || null);
    } else {
      setNombre('');
      setMusculo(MUSCLES[0]);
      setImagenUri(null);
    }
  }, [exerciseToEdit, visible]);

  const tipo = getTipoFromMusculo(musculo);

  const handlePickImage = async () => {
    try {
      const permissionResult = await ImagePicker.requestMediaLibraryPermissionsAsync();
      if (!permissionResult.granted) {
        Alert.alert('PERMISO REQUERIDO', 'Se requiere acceso a la galería para la imagen de ficha.');
        return;
      }

      const result = await ImagePicker.launchImageLibraryAsync({
        mediaTypes: ImagePicker.MediaTypeOptions.Images,
        allowsEditing: true,
        aspect: [4, 3],
        quality: 0.8,
      });

      if (!result.canceled && result.assets && result.assets.length > 0) {
        setImagenUri(result.assets[0].uri);
      }
    } catch (error) {
      Alert.alert('ERROR', 'No se pudo cargar la imagen.');
    }
  };

  const handleSubmit = () => {
    if (!nombre.trim()) {
      Alert.alert('CAMPO OBLIGATORIO', 'Ingresa el nombre del movimiento.');
      return;
    }

    onSave({
      id: isEditing ? exerciseToEdit.id : undefined,
      nombre: nombre.trim(),
      musculo,
      imagen: imagenUri,
    }, isEditing);

    setNombre('');
    setMusculo(MUSCLES[0]);
    setImagenUri(null);
    onClose();
  };

  return (
    <Modal visible={visible} animationType="fade" transparent={true} onRequestClose={onClose}>
      <View style={styles.modalOverlay}>
        <View style={styles.modalContent}>
          {/* Header */}
          <View style={styles.modalHeader}>
            <View>
              <Text style={styles.tagCode}>
                FORMULARIO DE REGISTRO // {isEditing ? 'EDICIÓN FICHA-EX' : 'ALTA FICHA-EX'}
              </Text>
              <Text style={styles.modalTitle}>
                {isEditing ? 'MODIFICAR MOVIMIENTO' : 'ALTA DE MOVIMIENTO'}
              </Text>
            </View>
            <TouchableOpacity onPress={onClose} style={styles.closeButton}>
              <Ionicons name="close-sharp" size={20} color={COLORS.softBlack} />
            </TouchableOpacity>
          </View>

          <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.formScroll}>
            <Text style={styles.label}>NOMBRE DEL MOVIMIENTO *</Text>
            <TextInput
              style={styles.input}
              placeholder="EJ. PRESS DE BANCA INCLINADO..."
              placeholderTextColor={COLORS.steelGray}
              value={nombre}
              onChangeText={setNombre}
            />

            <Text style={styles.label}>MÚSCULO PRINCIPAL *</Text>
            <View style={styles.musclesGrid}>
              {MUSCLES.map((m) => (
                <TouchableOpacity
                  key={m}
                  style={[styles.muscleChip, musculo === m && styles.muscleChipActive]}
                  onPress={() => setMusculo(m)}
                >
                  <Text style={[styles.muscleChipText, musculo === m && styles.muscleChipTextActive]}>
                    {m.toUpperCase()}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>

            <View style={styles.tipoContainer}>
              <Text style={styles.tipoLabel}>CATEGORÍA TÉCNICA ASIGNADA:</Text>
              <Text style={styles.tipoBadgeText}>{tipo.toUpperCase()}</Text>
            </View>

            <Text style={styles.label}>IMAGEN O ESQUEMA DE MOVIMIENTO</Text>
            <TouchableOpacity style={styles.imagePickerButton} onPress={handlePickImage}>
              {imagenUri ? (
                <View style={styles.previewContainer}>
                  <Image source={{ uri: imagenUri }} style={styles.imagePreview} resizeMode="cover" />
                  <Text style={styles.changeImageText}>[ CAMBIAR IMAGEN DE FICHA ]</Text>
                </View>
              ) : (
                <View style={styles.imagePickerPlaceholder}>
                  <Ionicons name="camera-outline" size={28} color={COLORS.softBlack} />
                  <Text style={styles.imagePickerText}>SELECCIONAR ARCHIVO DE IMAGEN LOCAL</Text>
                </View>
              )}
            </TouchableOpacity>
          </ScrollView>

          {/* Buttons */}
          <View style={styles.modalFooter}>
            <TouchableOpacity style={GLOBAL_STYLES.buttonSecondary} onPress={onClose}>
              <Text style={GLOBAL_STYLES.buttonSecondaryText}>CANCELAR</Text>
            </TouchableOpacity>

            <TouchableOpacity style={GLOBAL_STYLES.buttonPrimary} onPress={handleSubmit}>
              <Text style={GLOBAL_STYLES.buttonPrimaryText}>
                {isEditing ? 'GUARDAR CAMBIOS' : 'GUARDAR FICHA'}
              </Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(32, 35, 33, 0.75)',
    justifyContent: 'center',
    padding: 16,
  },
  modalContent: {
    backgroundColor: COLORS.paperCard,
    borderWidth: 2,
    borderColor: COLORS.softBlack,
    padding: 16,
    maxHeight: '85%',
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    borderBottomWidth: 1.5,
    borderBottomColor: COLORS.softBlack,
    paddingBottom: 10,
    marginBottom: 12,
  },
  tagCode: {
    fontFamily: FONTS.codeMono,
    fontSize: 9,
    color: COLORS.steelGray,
  },
  modalTitle: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 24,
    fontWeight: '900',
    color: COLORS.softBlack,
  },
  closeButton: {
    padding: 4,
  },
  formScroll: {
    paddingBottom: 10,
  },
  label: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 11,
    fontWeight: '800',
    color: COLORS.steelGray,
    marginBottom: 6,
    marginTop: 10,
  },
  input: {
    backgroundColor: COLORS.paper,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontFamily: FONTS.titleCondensed,
    color: COLORS.softBlack,
    fontSize: 14,
    fontWeight: '700',
  },
  musclesGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 6,
    marginVertical: 4,
  },
  muscleChip: {
    backgroundColor: COLORS.paper,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  muscleChipActive: {
    backgroundColor: COLORS.petroleum,
  },
  muscleChipText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.softBlack,
    fontSize: 11,
    fontWeight: '800',
  },
  muscleChipTextActive: {
    color: COLORS.paper,
  },
  tipoContainer: {
    backgroundColor: COLORS.paperDark,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
    padding: 10,
    marginVertical: 10,
  },
  tipoLabel: {
    fontFamily: FONTS.codeMono,
    color: COLORS.steelGray,
    fontSize: 9,
  },
  tipoBadgeText: {
    fontFamily: FONTS.metricCondensed,
    color: COLORS.petroleum,
    fontSize: 18,
    fontWeight: '900',
    marginTop: 2,
  },
  imagePickerButton: {
    backgroundColor: COLORS.paper,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    borderStyle: 'dashed',
    padding: 12,
    alignItems: 'center',
  },
  imagePickerPlaceholder: {
    alignItems: 'center',
    gap: 6,
  },
  imagePickerText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.steelGray,
    fontSize: 11,
    fontWeight: '700',
  },
  previewContainer: {
    alignItems: 'center',
    width: '100%',
  },
  imagePreview: {
    width: '100%',
    height: 120,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
  },
  changeImageText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.brickRed,
    fontSize: 11,
    fontWeight: '800',
    marginTop: 6,
  },
  modalFooter: {
    flexDirection: 'row',
    gap: 8,
    marginTop: 14,
    paddingTop: 10,
    borderTopWidth: 1.5,
    borderTopColor: COLORS.softBlack,
  },
});
