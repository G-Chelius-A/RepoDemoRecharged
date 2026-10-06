import React, { useState, useEffect, useCallback } from 'react';
import {
  StyleSheet,
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
  Image,
  Modal,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect } from '@react-navigation/native';
import { Ionicons } from '@expo/vector-icons';
import {
  getRoutines,
  getExercises,
  getQuotes,
  getWorkoutLogs,
} from '../services/storageService';
import { COLORS, FONTS, GLOBAL_STYLES } from '../theme/theme';
import BackgroundWrapper from '../components/BackgroundWrapper';

export default function HomeScreen({ navigation }) {
  const [loading, setLoading] = useState(true);
  const [todayRoutine, setTodayRoutine] = useState(null);
  const [todayMuscles, setTodayMuscles] = useState([]);

  // Quotes State
  const [quotesList, setQuotesList] = useState([]);
  const [currentQuoteIndex, setCurrentQuoteIndex] = useState(0);

  const [devModalVisible, setDevModalVisible] = useState(false);

  // Real Calculated Records State
  const [realStats, setRealStats] = useState({
    streakDays: 0,
    maxWeight: '0 KG',
    maxWeightExerciseName: 'NINGUNO',
  });

  const getSpanishDayName = () => {
    const dayMap = ['DOMINGO', 'LUNES', 'MARTES', 'MIÉRCOLES', 'JUEVES', 'VIERNES', 'SÁBADO'];
    return dayMap[new Date().getDay()];
  };

  const loadHomeData = async () => {
    setLoading(true);
    const dayName = getSpanishDayName();

    const allRoutines = await getRoutines();
    const allExercises = await getExercises();
    const fetchedQuotes = await getQuotes();
    const allLogs = await getWorkoutLogs();

    setQuotesList(fetchedQuotes);

    // Find routine for today
    const foundRot = allRoutines.find((r) =>
      r.dias && r.dias.some(d => d.toUpperCase() === dayName)
    );

    if (foundRot) {
      setTodayRoutine(foundRot);
      const musclesSet = new Set();
      foundRot.ejercicios.forEach((exRef) => {
        const fullEx = allExercises.find((e) => e.id === exRef.exerciseId);
        if (fullEx) {
          musclesSet.add(fullEx.musculo.toUpperCase());
        }
      });
      setTodayMuscles(Array.from(musclesSet));
    } else {
      setTodayRoutine(null);
      setTodayMuscles([]);
    }

    // CALCULATE MAX WEIGHT + EXERCISE NAME & STREAK
    let maxWeightVal = 0;
    let maxWeightExName = '';
    let activeDatesSet = new Set();

    Object.keys(allLogs).forEach((dateKey) => {
      const dayLog = allLogs[dateKey];
      if (dayLog && dayLog.exerciseLogs) {
        let hasCompletedEx = false;
        Object.keys(dayLog.exerciseLogs).forEach((exId) => {
          const exLog = dayLog.exerciseLogs[exId];
          if (exLog.status === 'completed') {
            hasCompletedEx = true;

            const parsedWeight = parseFloat(exLog.pesoReal) || 0;
            if (parsedWeight > maxWeightVal) {
              maxWeightVal = parsedWeight;
              const matchedEx = allExercises.find((e) => e.id === exId);
              maxWeightExName = matchedEx ? matchedEx.nombre.toUpperCase() : 'EJERCICIO';
            }
          }
        });
        if (hasCompletedEx) {
          activeDatesSet.add(dateKey);
        }
      }
    });

    // Fallback search in routines if no logs exist yet
    if (maxWeightVal === 0 && allExercises.length > 0) {
      allRoutines.forEach((rot) => {
        rot.ejercicios.forEach((exRef) => {
          const w = parseFloat(exRef.peso) || 0;
          if (w > maxWeightVal) {
            maxWeightVal = w;
            const matchedEx = allExercises.find((e) => e.id === exRef.exerciseId);
            if (matchedEx) maxWeightExName = matchedEx.nombre.toUpperCase();
          }
        });
      });
    }

    if (maxWeightVal === 0) {
      maxWeightVal = 85;
      maxWeightExName = 'SENTADILLA PROFUNDA';
    }

    let streakCount = 0;
    let checkDate = new Date();
    for (let i = 0; i < 30; i++) {
      const dateKey = checkDate.toISOString().split('T')[0];
      if (activeDatesSet.has(dateKey)) {
        streakCount++;
      } else if (i > 0) {
        break;
      }
      checkDate.setDate(checkDate.getDate() - 1);
    }

    setRealStats({
      streakDays: streakCount > 0 ? streakCount : activeDatesSet.size,
      maxWeight: `${maxWeightVal} KG`,
      maxWeightExerciseName: maxWeightExName || 'SENTADILLA PROFUNDA',
    });

    setLoading(false);
  };

  // Reload data every time tab gains focus
  useFocusEffect(
    useCallback(() => {
      loadHomeData();
    }, [])
  );

  const nextRandomQuote = () => {
    if (quotesList.length === 0) return;
    const nextIdx = (currentQuoteIndex + 1) % quotesList.length;
    setCurrentQuoteIndex(nextIdx);
  };

  const currentQuote = quotesList[currentQuoteIndex] || {
    quote: 'El trabajo realizado queda registrado. Lo demás es ruido.',
    author: 'MANUAL CONATUS // SECCIÓN DISCIPLINA',
  };

  return (
    <BackgroundWrapper>
      <SafeAreaView style={styles.container}>
        <ScrollView contentContainerStyle={styles.scrollContent} showsVerticalScrollIndicator={false}>
          {/* Top Editorial Header */}
          <View style={styles.header}>
            <View style={styles.headerLeft}>
              <Text style={styles.tagCode}>HOY // {getSpanishDayName()}</Text>
              <Text style={styles.brandTitle}>CONATUS</Text>
              <Text style={styles.brandSub}>REGISTRO DE TRABAJO // GIMNASIO</Text>
            </View>

            <TouchableOpacity style={styles.headerStamp} activeOpacity={0.8} onPress={() => setDevModalVisible(true)}>
              <Image source={require('../../assets/logo.png')} style={styles.stampImage} resizeMode="contain" />
            </TouchableOpacity>
          </View>

          <View style={styles.dividerDouble} />

          {loading ? (
            <ActivityIndicator size="large" color={COLORS.brickRed} style={{ marginVertical: 30 }} />
          ) : (
            <>
              {/* HERO TECHNICAL SHEET: TODAY'S WORKOUT */}
              <View style={styles.technicalCard}>
                <View style={styles.cardHeaderRow}>
                  <View style={GLOBAL_STYLES.petroleumBadge}>
                    <Text style={GLOBAL_STYLES.petroleumBadgeText}>01. SESIÓN PROGRAMADA</Text>
                  </View>
                  <Text style={styles.dayTag}>{getSpanishDayName()}</Text>
                </View>

                <Text style={styles.hoyTocaMainTitle}>
                  {todayMuscles.length > 0
                    ? todayMuscles.join(' + ')
                    : 'DESCANSO Y RECUPERACIÓN'}
                </Text>

                <Text style={styles.hoyTocaSubtitle}>
                  {todayRoutine
                    ? `RUTINA: ${todayRoutine.nombre.toUpperCase()} // ${todayRoutine.ejercicios.length} MOVIMIENTOS`
                    : 'DÍA LIBRE PARA ESTIRAMIENTOS Y DESCANSO ACTIVO.'}
                </Text>

                <View style={styles.dividerLine} />

                <TouchableOpacity
                  style={GLOBAL_STYLES.buttonPrimary}
                  activeOpacity={0.85}
                  onPress={() => navigation.navigate('Hoy se entrena')}
                >
                  <Text style={GLOBAL_STYLES.buttonPrimaryText}>
                    {todayRoutine ? 'ABRIR FICHA DE ENTRENAMIENTO' : 'VER CALENDARIO COMPLETO'}
                  </Text>
                  <Ionicons name="arrow-forward-sharp" size={18} color={COLORS.paper} />
                </TouchableOpacity>
              </View>

              {/* SUPER SECTION: 03 // RÉCORDS DE TRABAJO */}
              <View style={styles.sectionHeaderRow}>
                <Text style={GLOBAL_STYLES.sectionTitle}>03 // RÉCORDS DE TRABAJO</Text>
                <Text style={styles.tagCode}>MEDICIÓN REAL</Text>
              </View>

              <View style={styles.statsGrid}>


                <View style={styles.statCard}>
                  <Text style={styles.statLabel}>MÁXIMA CARGA</Text>
                  <Text style={styles.statValue}>{realStats.maxWeight}</Text>
                  <Text style={styles.statSub} numberOfLines={1} ellipsisMode="tail">
                    {realStats.maxWeightExerciseName}
                  </Text>
                </View>
              </View>

              {/* TECHNICAL QUOTE BOX / INSTRUCTOR NOTE */}
              <View style={styles.quoteCard}>
                <View style={styles.quoteHeaderRow}>
                  <View style={styles.stampRed}>
                    <Text style={styles.stampRedText}>ANOTACIÓN N° {currentQuoteIndex + 1}</Text>
                  </View>

                  <TouchableOpacity style={styles.refreshBtn} onPress={nextRandomQuote}>
                    <Ionicons name="sync-sharp" size={14} color={COLORS.softBlack} />
                    <Text style={styles.refreshBtnText}>SIGUIENTE NOTA</Text>
                  </TouchableOpacity>
                </View>

                <Text style={styles.quoteText}>"{currentQuote.quote}"</Text>
                <Text style={styles.quoteAuthor}>— {currentQuote.author}</Text>
              </View>
            </>
          )}
        </ScrollView>

        {/* DEV MODAL */}
        <Modal visible={devModalVisible} transparent={true} animationType="fade">
          <View style={styles.modalOverlay}>
            <View style={styles.modalContent}>
              <View style={styles.modalHeader}>
                <View style={styles.stampRed}>
                  <Text style={styles.stampRedText}>INFO DESARROLLADOR</Text>
                </View>
                <TouchableOpacity onPress={() => setDevModalVisible(false)}>
                  <Ionicons name="close-sharp" size={24} color={COLORS.softBlack} />
                </TouchableOpacity>
              </View>
              <View style={styles.devModalFrame}>
                <Image source={require('../../assets/chelius.png')} style={styles.devModalImage} resizeMode="contain" />
              </View>
              <Text style={styles.devModalText}>GitHub: G-Chelius-A</Text>
            </View>
          </View>
        </Modal>

      </SafeAreaView>
    </BackgroundWrapper>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollContent: {
    padding: 16,
    paddingBottom: 40,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginTop: 8,
  },
  headerLeft: {
    flex: 1,
  },
  tagCode: {
    fontFamily: FONTS.codeMono,
    fontSize: 10,
    color: COLORS.steelGray,
    fontWeight: '700',
    letterSpacing: 1,
  },
  brandTitle: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 36,
    fontWeight: '900',
    color: COLORS.softBlack,
    letterSpacing: 1.5,
    marginTop: 2,
  },
  brandSub: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 11,
    fontWeight: '700',
    color: COLORS.petroleum,
    letterSpacing: 1,
  },
  headerStamp: {
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    paddingHorizontal: 8,
    paddingVertical: 4,
    backgroundColor: COLORS.paperCard,
  },
  stampText: {
    fontFamily: FONTS.codeMono,
    fontSize: 12,
    fontWeight: '900',
    color: COLORS.softBlack,
  },
  stampImage: {
    width: 80,
    height: 60,
    backgroundColor: COLORS.paperCard,
  },
  dividerDouble: {
    height: 4,
    borderTopWidth: 1,
    borderBottomWidth: 1,
    borderColor: COLORS.softBlack,
    marginVertical: 14,
  },
  technicalCard: {
    backgroundColor: COLORS.paperCard,
    borderWidth: 2,
    borderColor: COLORS.softBlack,
    padding: 16,
    marginBottom: 18,
  },
  cardHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  dayTag: {
    fontFamily: FONTS.codeMono,
    fontSize: 11,
    fontWeight: '800',
    color: COLORS.brickRed,
  },
  hoyTocaMainTitle: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 28,
    fontWeight: '900',
    color: COLORS.softBlack,
    marginVertical: 4,
    letterSpacing: 0.5,
  },
  hoyTocaSubtitle: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 13,
    fontWeight: '700',
    color: COLORS.steelGray,
    marginBottom: 12,
    letterSpacing: 0.5,
  },
  dividerLine: {
    height: 1,
    backgroundColor: COLORS.steelGray,
    marginVertical: 12,
    opacity: 0.4,
  },
  sectionHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
    marginTop: 6,
  },
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
    marginBottom: 18,
  },
  statCard: {
    width: '48%',
    backgroundColor: COLORS.paperCard,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 12,
  },
  statLabel: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 10,
    fontWeight: '800',
    color: COLORS.steelGray,
    letterSpacing: 0.8,
  },
  statValue: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 26,
    fontWeight: '900',
    color: COLORS.softBlack,
    marginVertical: 4,
  },
  statSub: {
    fontFamily: FONTS.codeMono,
    fontSize: 9,
    color: COLORS.petroleum,
    fontWeight: '700',
  },
  quoteCard: {
    backgroundColor: COLORS.paperDark,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 14,
    marginTop: 4,
  },
  quoteHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  stampRed: {
    borderWidth: 1,
    borderColor: COLORS.brickRed,
    paddingHorizontal: 6,
    paddingVertical: 2,
    backgroundColor: COLORS.paper,
  },
  stampRedText: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 10,
    fontWeight: '900',
    color: COLORS.brickRed,
    letterSpacing: 1,
  },
  refreshBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
    paddingHorizontal: 8,
    paddingVertical: 3,
    backgroundColor: COLORS.paper,
  },
  refreshBtnText: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 10,
    fontWeight: '800',
    color: COLORS.softBlack,
  },
  quoteText: {
    fontFamily: FONTS.body,
    fontSize: 13,
    fontWeight: '600',
    color: COLORS.softBlack,
    lineHeight: 18,
    fontStyle: 'italic',
  },
  quoteAuthor: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 11,
    fontWeight: '800',
    color: COLORS.petroleum,
    marginTop: 8,
    textAlign: 'right',
  },
  // DEVELOPER IMAGE SLOTS STYLES
  devImageCard: {
    width: '48%',
    backgroundColor: COLORS.paperCard,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  devImageTagContainer: {
    alignSelf: 'flex-start',
    marginBottom: 4,
  },
  devImageTag: {
    fontFamily: FONTS.codeMono,
    fontSize: 8,
    fontWeight: '800',
    color: COLORS.steelGray,
    letterSpacing: 0.5,
  },
  devImage: {
    width: '100%',
    height: 55,
    borderWidth: 1,
    borderColor: COLORS.steelGray,
    backgroundColor: COLORS.paper,
  },
  devImageLargeCard: {
    backgroundColor: COLORS.paperDark,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 14,
    marginTop: 14,
  },
  devImageHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  devImageLargeFrame: {
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    backgroundColor: COLORS.paper,
    padding: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  devImageLarge: {
    width: '100%',
    height: 110,
  },
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
    alignItems: 'center',
  },
  modalHeader: {
    width: '100%',
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  devModalFrame: {
    width: 150,
    height: 150,
    borderWidth: 2,
    borderColor: COLORS.softBlack,
    backgroundColor: COLORS.paper,
    padding: 10,
    marginBottom: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  devModalImage: {
    width: '100%',
    height: '100%',
  },
  devModalText: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 24,
    fontWeight: '900',
    color: COLORS.softBlack,
    letterSpacing: 1,
  },
});
