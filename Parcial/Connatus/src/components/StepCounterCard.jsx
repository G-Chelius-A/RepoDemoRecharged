import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
  ActivityIndicator,
  Platform,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Pedometer } from 'expo-sensors';
import { COLORS, FONTS, GLOBAL_STYLES } from '../theme/theme';

export default function StepCounterCard() {
  const [isAvailable, setIsAvailable] = useState('checking');
  const [permissionStatus, setPermissionStatus] = useState('undetermined');
  const [liveSteps, setLiveSteps] = useState(0);
  const [pastSteps, setPastSteps] = useState(0);
  const [subscription, setSubscription] = useState(null);
  const [dailyGoal] = useState(10000);

  const initPedometer = async () => {
    try {
      setIsAvailable('checking');
      const available = await Pedometer.isAvailableAsync();
      setIsAvailable(available ? 'available' : 'unavailable');

      if (!available) {
        return;
      }

      // Check / request permission
      const perm = await Pedometer.getPermissionsAsync();
      let granted = perm.granted;

      if (!granted && perm.canAskAgain) {
        const req = await Pedometer.requestPermissionsAsync();
        granted = req.granted;
        setPermissionStatus(req.status);
      } else {
        setPermissionStatus(perm.status);
      }

      if (granted) {
        startPedometerTracking();
      }
    } catch (err) {
      console.warn('Error al inicializar pedómetro:', err);
      setIsAvailable('unavailable');
    }
  };

  const requestPermissionsManually = async () => {
    try {
      const req = await Pedometer.requestPermissionsAsync();
      setPermissionStatus(req.status);
      if (req.granted) {
        startPedometerTracking();
      }
    } catch (err) {
      console.warn('Error solicitando permisos:', err);
    }
  };

  const startPedometerTracking = async () => {
    // Get past steps from midnight today to now (if platform supports it)
    try {
      const start = new Date();
      start.setHours(0, 0, 0, 0);
      const end = new Date();

      const result = await Pedometer.getStepCountAsync(start, end);
      if (result && typeof result.steps === 'number') {
        setPastSteps(result.steps);
      }
    } catch (e) {
      // getStepCountAsync is mostly supported on iOS or certain Android builds
      console.log('Past steps query not supported or failed on this device');
    }

    // Subscribe to live step updates
    try {
      if (subscription) {
        subscription.remove();
      }
      const sub = Pedometer.watchStepCount((result) => {
        if (result && typeof result.steps === 'number') {
          setLiveSteps(result.steps);
        }
      });
      setSubscription(sub);
    } catch (e) {
      console.warn('Error al iniciar watchStepCount:', e);
    }
  };

  useEffect(() => {
    initPedometer();

    return () => {
      if (subscription) {
        subscription.remove();
      }
    };
  }, []);

  const totalSteps = pastSteps + liveSteps;
  const distanceKm = ((totalSteps * 0.76) / 1000).toFixed(2);
  const caloriesBurned = Math.round(totalSteps * 0.04);
  const progressPercent = Math.min(Math.round((totalSteps / dailyGoal) * 100), 100);

  return (
    <View style={styles.card}>
      {/* Header */}
      <View style={styles.cardHeader}>
        <View style={{ flex: 1 }}>
          <Text style={styles.tagCode}>03.1 // SENSOR DE ACTIVIDAD FÍSICA</Text>
          <Text style={styles.cardTitle}>CONTADOR DE PASOS</Text>
        </View>
        <View style={[
          styles.statusBadge,
          permissionStatus === 'granted' ? styles.statusActive : styles.statusInactive
        ]}>
          <Ionicons
            name={permissionStatus === 'granted' ? 'footsteps' : 'alert-circle-outline'}
            size={14}
            color={permissionStatus === 'granted' ? COLORS.paper : COLORS.steelGray}
          />
          <Text style={[
            styles.statusText,
            permissionStatus === 'granted' ? styles.statusTextActive : styles.statusTextInactive
          ]}>
            {permissionStatus === 'granted' ? 'SENSOR ACTIVO' : 'PERMISO REQUERIDO'}
          </Text>
        </View>
      </View>

      <View style={styles.divider} />

      {isAvailable === 'checking' ? (
        <View style={styles.loadingBox}>
          <ActivityIndicator size="small" color={COLORS.brickRed} />
          <Text style={styles.loadingText}>VERIFICANDO SENSOR DE MOVIMIENTO...</Text>
        </View>
      ) : isAvailable === 'unavailable' ? (
        <View style={styles.unavailableBox}>
          <Ionicons name="hardware-chip-outline" size={24} color={COLORS.steelGray} />
          <Text style={styles.unavailableTitle}>HARDWARE PEDÓMETRO NO DETECTADO</Text>
          <Text style={styles.unavailableSub}>
            ESTE DISPOSITIVO O EMULADOR NO DISPONE DE UN SENSOR HARDWARE DE PASOS ACTIVO.
          </Text>
          <TouchableOpacity
            style={[GLOBAL_STYLES.buttonSecondary, { marginTop: 10 }]}
            onPress={() => setLiveSteps((prev) => prev + 250)}
          >
            <Ionicons name="add-circle-outline" size={16} color={COLORS.softBlack} />
            <Text style={GLOBAL_STYLES.buttonSecondaryText}>SIMULAR +250 PASOS (DEMO)</Text>
          </TouchableOpacity>
        </View>
      ) : permissionStatus !== 'granted' ? (
        <View style={styles.permissionPromptBox}>
          <Ionicons name="shield-checkmark-outline" size={28} color={COLORS.brickRed} />
          <Text style={styles.permissionTitle}>ACCESO A ACTIVIDAD FÍSICA</Text>
          <Text style={styles.permissionSub}>
            CONATUS REQUIERE PERMISO PARA LEER LOS SENSORES DE ACTIVIDAD Y CONTAR TUS PASOS EN TIEMPO REAL.
          </Text>
          <TouchableOpacity
            style={[GLOBAL_STYLES.buttonPrimary, { width: '100%', marginTop: 8 }]}
            onPress={requestPermissionsManually}
          >
            <Text style={GLOBAL_STYLES.buttonPrimaryText}>OTORGAR PERMISOS DE ACTIVIDAD</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <>
          {/* Main Counter Display */}
          <View style={styles.mainMetricsRow}>
            <View style={styles.stepsBigBox}>
              <Text style={styles.stepsLabel}>PASOS REGISTRADOS HOY</Text>
              <Text style={styles.stepsValue}>
                {totalSteps.toLocaleString()}
              </Text>
              <Text style={styles.stepsSub}>
                META DIARIA: {dailyGoal.toLocaleString()} PASOS ({progressPercent}%)
              </Text>
            </View>
          </View>

          {/* Progress Bar */}
          <View style={styles.progressContainer}>
            <View style={styles.progressBarBg}>
              <View style={[styles.progressBarFill, { width: `${progressPercent}%` }]} />
            </View>
          </View>

          {/* Secondary stats */}
          <View style={styles.statsRow}>
            <View style={styles.subStatCard}>
              <Ionicons name="walk-outline" size={16} color={COLORS.brickRed} />
              <Text style={styles.subStatVal}>{distanceKm} KM</Text>
              <Text style={styles.subStatLabel}>DISTANCIA EST.</Text>
            </View>

            <View style={styles.subStatCard}>
              <Ionicons name="flame-outline" size={16} color={COLORS.brickRed} />
              <Text style={styles.subStatVal}>{caloriesBurned} KCAL</Text>
              <Text style={styles.subStatLabel}>CALORÍAS EST.</Text>
            </View>

            <View style={styles.subStatCard}>
              <Ionicons name="pulse-outline" size={16} color={COLORS.brickRed} />
              <Text style={styles.subStatVal}>+{liveSteps}</Text>
              <Text style={styles.subStatLabel}>EN SESIÓN</Text>
            </View>
          </View>

          <Text style={styles.footerNote}>
            SENSOR INTEGRADO // SEGUIMIENTO CONTINUO DE ACTIVIDAD FÍSICA
          </Text>
        </>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: COLORS.paperCard,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 14,
    marginBottom: 16,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    gap: 8,
  },
  tagCode: {
    fontFamily: FONTS.codeMono,
    fontSize: 9,
    color: COLORS.steelGray,
    fontWeight: '700',
  },
  cardTitle: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 20,
    fontWeight: '900',
    color: COLORS.softBlack,
    marginTop: 1,
  },
  statusBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
  },
  statusActive: {
    backgroundColor: COLORS.petroleum,
  },
  statusInactive: {
    backgroundColor: COLORS.paperDark,
  },
  statusText: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 9,
    fontWeight: '800',
  },
  statusTextActive: {
    color: COLORS.paper,
  },
  statusTextInactive: {
    color: COLORS.steelGray,
  },
  divider: {
    height: 1,
    backgroundColor: COLORS.softBlack,
    marginVertical: 12,
  },
  loadingBox: {
    padding: 20,
    alignItems: 'center',
    gap: 8,
  },
  loadingText: {
    fontFamily: FONTS.codeMono,
    fontSize: 10,
    color: COLORS.steelGray,
  },
  unavailableBox: {
    backgroundColor: COLORS.paperDark,
    borderWidth: 1,
    borderColor: COLORS.steelGray,
    padding: 14,
    alignItems: 'center',
    gap: 6,
  },
  unavailableTitle: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 15,
    fontWeight: '900',
    color: COLORS.softBlack,
  },
  unavailableSub: {
    fontFamily: FONTS.codeMono,
    fontSize: 9,
    color: COLORS.steelGray,
    textAlign: 'center',
  },
  permissionPromptBox: {
    backgroundColor: COLORS.paperDark,
    borderWidth: 1,
    borderColor: COLORS.brickRed,
    padding: 14,
    alignItems: 'center',
    gap: 6,
  },
  permissionTitle: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 16,
    fontWeight: '900',
    color: COLORS.softBlack,
  },
  permissionSub: {
    fontFamily: FONTS.codeMono,
    fontSize: 9,
    color: COLORS.steelGray,
    textAlign: 'center',
    lineHeight: 13,
  },
  mainMetricsRow: {
    marginBottom: 8,
  },
  stepsBigBox: {
    alignItems: 'center',
    paddingVertical: 6,
  },
  stepsLabel: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 11,
    fontWeight: '800',
    color: COLORS.steelGray,
    letterSpacing: 1,
  },
  stepsValue: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 42,
    fontWeight: '900',
    color: COLORS.brickRed,
    marginVertical: -2,
  },
  stepsSub: {
    fontFamily: FONTS.codeMono,
    fontSize: 9,
    color: COLORS.petroleum,
    fontWeight: '700',
  },
  progressContainer: {
    marginBottom: 12,
  },
  progressBarBg: {
    height: 8,
    backgroundColor: COLORS.paperDark,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
  },
  progressBarFill: {
    height: '100%',
    backgroundColor: COLORS.brickRed,
  },
  statsRow: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 8,
  },
  subStatCard: {
    flex: 1,
    backgroundColor: COLORS.paper,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
    padding: 8,
    alignItems: 'center',
    gap: 2,
  },
  subStatVal: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 16,
    fontWeight: '900',
    color: COLORS.softBlack,
  },
  subStatLabel: {
    fontFamily: FONTS.codeMono,
    fontSize: 8,
    color: COLORS.steelGray,
  },
  footerNote: {
    fontFamily: FONTS.codeMono,
    fontSize: 8,
    color: COLORS.steelGray,
    textAlign: 'center',
    marginTop: 4,
  },
});
