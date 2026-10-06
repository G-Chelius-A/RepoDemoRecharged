import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
  StyleSheet,
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
  Dimensions,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect } from '@react-navigation/native';
import { Ionicons } from '@expo/vector-icons';
import { captureRef } from 'react-native-view-shot';
import * as Sharing from 'expo-sharing';
import { LineChart } from 'react-native-chart-kit';
import {
  getWorkoutLogs,
  getRoutines,
  getExercises,
} from '../services/storageService';
import UpdateWeightModal from '../components/UpdateWeightModal';
import StepCounterCard from '../components/StepCounterCard';
import { COLORS, FONTS, GLOBAL_STYLES } from '../theme/theme';
import BackgroundWrapper from '../components/BackgroundWrapper';

const screenWidth = Dimensions.get('window').width;

export default function StatsScreen() {
  const [loading, setLoading] = useState(true);
  const [timeFilter, setTimeFilter] = useState('Semanal');
  
  const [selectedExerciseId, setSelectedExerciseId] = useState('');
  const [metricMode, setMetricMode] = useState('peso'); // 'peso' | 'reps'

  const [routines, setRoutines] = useState([]);
  const [exercises, setExercises] = useState([]);
  const [logs, setLogs] = useState({});

  const [updateWeightModalVisible, setUpdateWeightModalVisible] = useState(false);

  const reportCardRef = useRef();

  const loadData = async () => {
    setLoading(true);
    const rotData = await getRoutines();
    const exData = await getExercises();
    const logsData = await getWorkoutLogs();

    setRoutines(rotData);
    setExercises(exData);
    setLogs(logsData);

    if (exData.length > 0 && !selectedExerciseId) {
      setSelectedExerciseId(exData[0].id);
    }
    setLoading(false);
  };

  useFocusEffect(
    useCallback(() => {
      loadData();
    }, [])
  );

  const handleDownloadPNG = async () => {
    try {
      if (!reportCardRef.current) return;
      const uri = await captureRef(reportCardRef, {
        format: 'png',
        quality: 0.9,
      });

      if (await Sharing.isAvailableAsync()) {
        await Sharing.shareAsync(uri, {
          mimeType: 'image/png',
          dialogTitle: 'Compartir Ficha de Evolución CONATUS',
        });
      } else {
        Alert.alert('FICHA GENERADA', `Imagen guardada en:\n${uri}`);
      }
    } catch (error) {
      console.error('Error al capturar PNG:', error);
      Alert.alert('ERROR', 'No se pudo exportar la ficha gráfica.');
    }
  };

  const getExerciseRealHistory = () => {
    if (!selectedExerciseId) return { labels: [], data: [] };

    const points = [];
    const dateKeys = Object.keys(logs).sort();

    dateKeys.forEach((dateKey) => {
      const dayLog = logs[dateKey];
      if (dayLog && dayLog.exerciseLogs && dayLog.exerciseLogs[selectedExerciseId]) {
        const exLog = dayLog.exerciseLogs[selectedExerciseId];
        if (exLog.status === 'completed') {
          const [yr, mo, da] = dateKey.split('-');
          const label = `${da}/${mo}`;

          if (metricMode === 'peso') {
            const val = parseFloat(exLog.pesoReal) || 0;
            if (val > 0) {
              points.push({ label, val });
            }
          } else {
            let repsSum = 0;
            if (exLog.seriesReps && Array.isArray(exLog.seriesReps)) {
              exLog.seriesReps.forEach((s) => {
                repsSum += parseInt(s.reps, 10) || 0;
              });
            }
            if (repsSum > 0) {
              points.push({ label, val: repsSum });
            }
          }
        }
      }
    });

    if (points.length === 0) {
      return {
        labels: [],
        data: [],
        hasRealData: false,
      };
    }

    return {
      labels: points.map((p) => p.label),
      data: points.map((p) => p.val),
      hasRealData: true,
    };
  };

  const chartHistory = getExerciseRealHistory();
  const selectedExObject = exercises.find((e) => e.id === selectedExerciseId);

  const calculateOverallStats = () => {
    let totalReps = 0;
    let totalWeightAccumulated = 0;
    let completedSessions = 0;
    let weightsList = [];

    Object.keys(logs).forEach((dateKey) => {
      const dayLog = logs[dateKey];
      if (dayLog && dayLog.exerciseLogs) {
        let hasCompleted = false;
        Object.keys(dayLog.exerciseLogs).forEach((exId) => {
          const exLog = dayLog.exerciseLogs[exId];
          if (exLog.status === 'completed') {
            hasCompleted = true;
            if (exLog.seriesReps) {
              exLog.seriesReps.forEach((s) => {
                totalReps += parseInt(s.reps, 10) || 0;
              });
            }
            if (exLog.pesoReal) {
              const parsedWeight = parseFloat(exLog.pesoReal);
              if (!isNaN(parsedWeight) && parsedWeight > 0) {
                totalWeightAccumulated += parsedWeight;
                weightsList.push(parsedWeight);
              }
            }
          }
        });
        if (hasCompleted) completedSessions++;
      }
    });

    const avgWeight = weightsList.length > 0 ? (totalWeightAccumulated / weightsList.length).toFixed(1) : 0;
    const maxWeight = weightsList.length > 0 ? Math.max(...weightsList) : 0;

    return {
      totalReps: totalReps,
      avgWeight: avgWeight,
      maxWeight: maxWeight,
      completedSessions: completedSessions,
      hasData: completedSessions > 0 || totalReps > 0 || weightsList.length > 0,
    };
  };

  const overallStats = calculateOverallStats();

  return (
    <BackgroundWrapper>
      <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.scrollContent} showsVerticalScrollIndicator={false}>
        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.tagCode}>MÓDULO DE SEGUIMIENTO // EST-2026</Text>
          <Text style={styles.headerTitle}>REGISTRO DE PROGRESO</Text>
          <Text style={styles.headerSubtitle}>MÉTRICAS TÉCNICAS Y CONTROL DE CARGA</Text>
        </View>

        {/* Filter */}
        <View style={styles.filterContainer}>
          {['Semanal', 'Mensual', 'Histórico'].map((f) => (
            <TouchableOpacity
              key={f}
              style={[styles.filterBtn, timeFilter === f && styles.filterBtnActive]}
              onPress={() => setTimeFilter(f)}
            >
              <Text style={[styles.filterBtnText, timeFilter === f && styles.filterBtnTextActive]}>
                [ {f.toUpperCase()} ]
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Action Card */}
        <View style={styles.crudBanner}>
          <View style={styles.crudInfo}>
            <Text style={styles.crudTitle}>AJUSTAR PESO OBJETIVO</Text>
            <Text style={styles.crudSubtitle}>MODIFICA LOS KG ASIGNADOS EN LAS RUTINAS</Text>
          </View>
          <TouchableOpacity
            style={GLOBAL_STYLES.buttonPrimary}
            onPress={() => setUpdateWeightModalVisible(true)}
          >
            <Text style={GLOBAL_STYLES.buttonPrimaryText}>ACTUALIZAR PESO</Text>
          </TouchableOpacity>
        </View>

        {loading ? (
          <ActivityIndicator size="large" color={COLORS.brickRed} style={{ marginVertical: 30 }} />
        ) : (
          <>
            {/* Pedometer / Step Counter Card */}
            <StepCounterCard />

            {/* Chart Technical Box */}
            <View style={styles.chartCard}>
              <View style={styles.chartHeaderRow}>
                <Text style={styles.chartSectionTitle}>EVOLUCIÓN CONTINUA POR EJERCICIO</Text>
              </View>

              {/* Selector Chips */}
              <Text style={styles.selectorLabel}>SELECCIONAR MOVIMIENTO:</Text>
              <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.exChipsScroll}>
                <View style={styles.exChipsRow}>
                  {exercises.map((ex) => (
                    <TouchableOpacity
                      key={ex.id}
                      style={[
                        styles.exChip,
                        selectedExerciseId === ex.id && styles.exChipActive,
                      ]}
                      onPress={() => setSelectedExerciseId(ex.id)}
                    >
                      <Text
                        style={[
                          styles.exChipText,
                          selectedExerciseId === ex.id && styles.exChipTextActive,
                        ]}
                      >
                        {ex.nombre.toUpperCase()}
                      </Text>
                    </TouchableOpacity>
                  ))}
                </View>
              </ScrollView>

              {/* Metric Mode */}
              <View style={styles.metricSwitcher}>
                <TouchableOpacity
                  style={[styles.metricBtn, metricMode === 'peso' && styles.metricBtnActive]}
                  onPress={() => setMetricMode('peso')}
                >
                  <Text style={[styles.metricBtnText, metricMode === 'peso' && styles.metricBtnTextActive]}>
                    CARGAS REALES (KG)
                  </Text>
                </TouchableOpacity>

                <TouchableOpacity
                  style={[styles.metricBtn, metricMode === 'reps' && styles.metricBtnActive]}
                  onPress={() => setMetricMode('reps')}
                >
                  <Text style={[styles.metricBtnText, metricMode === 'reps' && styles.metricBtnTextActive]}>
                    REPETICIONES TOTALES
                  </Text>
                </TouchableOpacity>
              </View>

              {/* Line Chart or No Data Banner */}
              {chartHistory.hasRealData && chartHistory.data.length > 0 ? (
                <View style={styles.chartWrapper}>
                  <LineChart
                    data={{
                      labels: chartHistory.labels,
                      datasets: [
                        {
                          data: chartHistory.data,
                          color: () => COLORS.brickRed,
                          strokeWidth: 2.5,
                        },
                      ],
                    }}
                    width={screenWidth - 64}
                    height={190}
                    yAxisSuffix={metricMode === 'peso' ? ' KG' : ' R'}
                    chartConfig={{
                      backgroundColor: COLORS.paperCard,
                      backgroundGradientFrom: COLORS.paperCard,
                      backgroundGradientTo: COLORS.paperDark,
                      decimalPlaces: 0,
                      color: () => COLORS.brickRed,
                      labelColor: () => COLORS.softBlack,
                      propsForDots: {
                        r: '5',
                        strokeWidth: '2',
                        stroke: COLORS.softBlack,
                        fill: COLORS.brickRed,
                      },
                      propsForBackgroundLines: {
                        stroke: COLORS.steelLight,
                        strokeWidth: 1,
                      },
                    }}
                    bezier
                    style={styles.chartStyle}
                  />
                </View>
              ) : (
                <View style={styles.noDataBox}>
                  <Ionicons name="document-text-outline" size={30} color={COLORS.steelGray} />
                  <Text style={styles.noDataTitle}>SIN REGISTRO DE TRABAJO</Text>
                  <Text style={styles.noDataSub}>
                    NO HAY DATOS REGISTRADOS PARA ESTE EJERCICIO AÚN. REGISTRA UNA SESIÓN PARA VISUALIZAR TU EVOLUCIÓN REAL.
                  </Text>
                </View>
              )}

              <Text style={styles.chartFooterNote}>
                FICHA DE SEGUIMIENTO // {selectedExObject ? selectedExObject.nombre.toUpperCase() : ''}
              </Text>
            </View>

            {/* Metrics Grid */}
            <View style={styles.metricsGrid}>
              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>REPETICIONES TOTALES</Text>
                <Text style={styles.metricValue}>{overallStats.totalReps}</Text>
                <Text style={styles.metricSub}>ACUMULADAS EN SESIONES</Text>
              </View>

              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>CARGA PROMEDIO</Text>
                <Text style={styles.metricValue}>{overallStats.avgWeight} KG</Text>
                <Text style={styles.metricSub}>MEDIA DE EJERCICIOS</Text>
              </View>

              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>RECORD PERSONAL (PR)</Text>
                <Text style={styles.metricValue}>{overallStats.maxWeight} KG</Text>
                <Text style={styles.metricSub}>TOP CARGA REGISTRADA</Text>
              </View>

              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>SESIONES COMPLETADAS</Text>
                <Text style={styles.metricValue}>{overallStats.completedSessions}</Text>
                <Text style={styles.metricSub}>FICHA DE REGISTRO</Text>
              </View>
            </View>

            {/* Exportable Sheet */}
            <Text style={GLOBAL_STYLES.sectionTitle}>03 // FICHA EXPORTABLE DE RENDIMIENTO</Text>

            <View ref={reportCardRef} style={styles.reportCard} collapsable={false}>
              <View style={styles.reportHeader}>
                <Text style={styles.reportBrandTitle}>CONATUS // REPORTE SEMANAL</Text>
                <Text style={styles.reportDateBadge}>EDICIÓN 2026</Text>
              </View>

              <View style={styles.dividerDouble} />

              <View style={styles.reportBody}>
                <View style={styles.reportStatRow}>
                  <Text style={styles.reportStatKey}>SESIONES COMPLETADAS:</Text>
                  <Text style={styles.reportStatVal}>{overallStats.completedSessions} SESIONES</Text>
                </View>

                <View style={styles.reportStatRow}>
                  <Text style={styles.reportStatKey}>REPETICIONES TOTALES:</Text>
                  <Text style={styles.reportStatVal}>{overallStats.totalReps} REPS</Text>
                </View>

                <View style={styles.reportStatRow}>
                  <Text style={styles.reportStatKey}>CARGA MÁXIMA REGISTRADA:</Text>
                  <Text style={styles.reportStatVal}>{overallStats.maxWeight} KG</Text>
                </View>

                <View style={styles.reportStatRow}>
                  <Text style={styles.reportStatKey}>PROMEDIO DE PESO:</Text>
                  <Text style={styles.reportStatVal}>{overallStats.avgWeight} KG</Text>
                </View>
              </View>

              <View style={styles.dividerSingle} />

              <View style={styles.reportFooter}>
                <Text style={styles.reportFooterText}>
                  REGISTRO LOCAL IMPRESO DE CUMPLIMIENTO // SIN ADORNOS
                </Text>
              </View>
            </View>

            <TouchableOpacity style={GLOBAL_STYLES.buttonSecondary} onPress={handleDownloadPNG}>
              <Ionicons name="camera-sharp" size={16} color={COLORS.softBlack} />
              <Text style={GLOBAL_STYLES.buttonSecondaryText}>EXPORTAR IMAGEN DE FICHA (PNG)</Text>
            </TouchableOpacity>
          </>
        )}

        <UpdateWeightModal
          visible={updateWeightModalVisible}
          onClose={() => setUpdateWeightModalVisible(false)}
          routines={routines}
          exercises={exercises}
          onSaved={loadData}
        />
      </ScrollView>
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
    marginTop: 8,
    marginBottom: 12,
  },
  tagCode: {
    fontFamily: FONTS.codeMono,
    fontSize: 10,
    color: COLORS.steelGray,
    fontWeight: '700',
  },
  headerTitle: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 34,
    fontWeight: '900',
    color: COLORS.softBlack,
    letterSpacing: 1.5,
  },
  headerSubtitle: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 12,
    fontWeight: '700',
    color: COLORS.petroleum,
  },
  filterContainer: {
    flexDirection: 'row',
    backgroundColor: COLORS.paperCard,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 3,
    marginBottom: 14,
  },
  filterBtn: {
    flex: 1,
    paddingVertical: 8,
    alignItems: 'center',
  },
  filterBtnActive: {
    backgroundColor: COLORS.brickRed,
  },
  filterBtnText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.steelGray,
    fontSize: 12,
    fontWeight: '800',
  },
  filterBtnTextActive: {
    color: COLORS.paper,
  },
  crudBanner: {
    backgroundColor: COLORS.paperCard,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 14,
    marginBottom: 16,
    gap: 10,
  },
  crudInfo: {
    gap: 2,
  },
  crudTitle: {
    fontFamily: FONTS.metricCondensed,
    color: COLORS.softBlack,
    fontSize: 18,
    fontWeight: '900',
  },
  crudSubtitle: {
    fontFamily: FONTS.codeMono,
    color: COLORS.steelGray,
    fontSize: 9,
  },
  chartCard: {
    backgroundColor: COLORS.paperCard,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 14,
    marginBottom: 16,
  },
  chartHeaderRow: {
    marginBottom: 8,
  },
  chartSectionTitle: {
    fontFamily: FONTS.metricCondensed,
    color: COLORS.softBlack,
    fontSize: 18,
    fontWeight: '900',
  },
  selectorLabel: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.steelGray,
    fontSize: 10,
    fontWeight: '700',
    marginBottom: 6,
  },
  exChipsScroll: {
    marginBottom: 10,
  },
  exChipsRow: {
    flexDirection: 'row',
    gap: 6,
  },
  exChip: {
    backgroundColor: COLORS.paper,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
  },
  exChipActive: {
    backgroundColor: COLORS.petroleum,
  },
  exChipText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.softBlack,
    fontSize: 11,
    fontWeight: '800',
  },
  exChipTextActive: {
    color: COLORS.paper,
  },
  metricSwitcher: {
    flexDirection: 'row',
    borderWidth: 1,
    borderColor: COLORS.softBlack,
    backgroundColor: COLORS.paper,
    marginBottom: 12,
  },
  metricBtn: {
    flex: 1,
    paddingVertical: 8,
    alignItems: 'center',
  },
  metricBtnActive: {
    backgroundColor: COLORS.brickRed,
  },
  metricBtnText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.steelGray,
    fontSize: 11,
    fontWeight: '800',
  },
  metricBtnTextActive: {
    color: COLORS.paper,
  },
  chartWrapper: {
    alignItems: 'center',
    borderWidth: 1,
    borderColor: COLORS.softBlack,
  },
  chartStyle: {
    borderRadius: 0,
  },
  chartFooterNote: {
    fontFamily: FONTS.codeMono,
    color: COLORS.steelGray,
    fontSize: 9,
    marginTop: 8,
    textAlign: 'center',
  },
  metricsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
    marginBottom: 16,
  },
  metricCard: {
    width: '48%',
    backgroundColor: COLORS.paperCard,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 12,
  },
  metricLabel: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 10,
    fontWeight: '800',
    color: COLORS.steelGray,
  },
  metricValue: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 26,
    fontWeight: '900',
    color: COLORS.softBlack,
    marginVertical: 2,
  },
  metricSub: {
    fontFamily: FONTS.codeMono,
    fontSize: 9,
    color: COLORS.petroleum,
  },
  reportCard: {
    backgroundColor: COLORS.paperCard,
    borderWidth: 2,
    borderColor: COLORS.softBlack,
    padding: 16,
    marginBottom: 14,
  },
  reportHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  reportBrandTitle: {
    fontFamily: FONTS.metricCondensed,
    color: COLORS.softBlack,
    fontSize: 18,
    fontWeight: '900',
  },
  reportDateBadge: {
    fontFamily: FONTS.codeMono,
    color: COLORS.brickRed,
    fontSize: 10,
    fontWeight: '800',
  },
  dividerDouble: {
    height: 4,
    borderTopWidth: 1,
    borderBottomWidth: 1,
    borderColor: COLORS.softBlack,
    marginVertical: 10,
  },
  dividerSingle: {
    height: 1,
    backgroundColor: COLORS.steelGray,
    marginVertical: 10,
    opacity: 0.5,
  },
  reportBody: {
    gap: 8,
  },
  reportStatRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  reportStatKey: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.steelGray,
    fontSize: 12,
    fontWeight: '700',
  },
  reportStatVal: {
    fontFamily: FONTS.metricCondensed,
    color: COLORS.softBlack,
    fontSize: 16,
    fontWeight: '900',
  },
  reportFooter: {
    alignItems: 'center',
  },
  reportFooterText: {
    fontFamily: FONTS.codeMono,
    color: COLORS.petroleum,
    fontSize: 9,
    fontWeight: '700',
  },
  noDataBox: {
    backgroundColor: COLORS.paperDark,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 20,
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: 4,
  },
  noDataTitle: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 17,
    fontWeight: '900',
    color: COLORS.softBlack,
    marginTop: 6,
    letterSpacing: 1,
  },
  noDataSub: {
    fontFamily: FONTS.codeMono,
    fontSize: 9,
    color: COLORS.steelGray,
    textAlign: 'center',
    marginTop: 4,
    lineHeight: 14,
  },
});
