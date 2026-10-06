import { Platform, StyleSheet } from 'react-native';

export const COLORS = {
  // Paleta oficial CONATUS
  paper: '#F1E8D2',          // Crema / papel principal
  paperDark: '#E4DABF',      // Papel envejecido / celdas secundarias
  paperLight: '#FAF5EA',     // Papel técnico limpio
  paperCard: '#F6EFE0',      // Fondo de fichas técnicas
  
  petroleum: '#183A43',      // Azul petróleo primario
  petroleumLight: '#23505C', // Azul petróleo secundario
  petroleumDark: '#0F262C',  // Fondo oscuro técnico
  
  brickRed: '#A43D32',       // Rojo ladrillo (Énfasis / Sellos / Botones primarios)
  brickRedLight: '#BD483D',  // Rojo ladrillo interactivo
  brickRedMuted: '#802E25',  // Rojo ladrillo oscuro
  
  steelGray: '#77736B',      // Gris acero (Bordes técnicos, divisores, leyendas)
  steelLight: '#C3BEB4',     // Gris acero claro para cuadrícula
  steelDark: '#4A4843',      // Gris acero oscuro
  
  softBlack: '#202321',      // Negro suave (Texto principal, métricas pesadas)
  white: '#FFFFFF',
  transparent: 'transparent',
  
  // Indicadores de estado de ficha
  success: '#183A43',        // Verde/Petróleo de conformidad
  danger: '#A43D32',         // Rojo ladrillo de advertencia
  disabled: '#AFA99C',
};

export const FONTS = {
  titleCondensed: Platform.select({
    ios: 'AvenirNext-CondensedBold',
    android: 'sans-serif-condensed',
    web: '"Barlow Condensed", Oswald, "Arial Narrow", sans-serif',
    default: 'sans-serif-condensed',
  }),
  metricCondensed: Platform.select({
    ios: 'AvenirNext-CondensedHeavy',
    android: 'sans-serif-condensed',
    web: '"Barlow Condensed", Oswald, "Arial Narrow", sans-serif',
    default: 'sans-serif-condensed',
  }),
  body: Platform.select({
    ios: 'System',
    android: 'sans-serif',
    web: 'Inter, system-ui, -apple-system, sans-serif',
    default: 'sans-serif',
  }),
  codeMono: Platform.select({
    ios: 'Courier',
    android: 'monospace',
    web: '"Space Mono", Courier, monospace',
    default: 'monospace',
  }),
};

export const GLOBAL_STYLES = StyleSheet.create({
  // Tipografía editorial condensada
  headerTitle: {
    fontFamily: FONTS.titleCondensed,
    fontWeight: '800',
    fontSize: 24,
    color: COLORS.softBlack,
    textTransform: 'uppercase',
    letterSpacing: 1.2,
  },
  sectionTitle: {
    fontFamily: FONTS.titleCondensed,
    fontWeight: '700',
    fontSize: 16,
    color: COLORS.petroleum,
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
  tagCode: {
    fontFamily: FONTS.codeMono,
    fontSize: 10,
    color: COLORS.steelGray,
    letterSpacing: 1,
    textTransform: 'uppercase',
  },
  metricLarge: {
    fontFamily: FONTS.metricCondensed,
    fontWeight: '900',
    fontSize: 38,
    color: COLORS.softBlack,
    letterSpacing: -0.5,
  },
  metricUnit: {
    fontFamily: FONTS.titleCondensed,
    fontWeight: '700',
    fontSize: 14,
    color: COLORS.steelGray,
    textTransform: 'uppercase',
    marginLeft: 4,
  },
  
  // Elementos utilitarios y divisores de libreta
  technicalCard: {
    backgroundColor: COLORS.paperCard,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    borderRadius: 2,
    padding: 14,
    marginBottom: 14,
  },
  technicalHeader: {
    borderBottomWidth: 1.5,
    borderBottomColor: COLORS.softBlack,
    paddingBottom: 8,
    marginBottom: 12,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  stampBadge: {
    borderWidth: 1.5,
    borderColor: COLORS.brickRed,
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 2,
    alignSelf: 'flex-start',
    backgroundColor: COLORS.paper,
    transform: [{ rotate: '-1.5deg' }],
  },
  stampBadgeText: {
    fontFamily: FONTS.titleCondensed,
    fontWeight: '800',
    fontSize: 11,
    color: COLORS.brickRed,
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
  petroleumBadge: {
    backgroundColor: COLORS.petroleum,
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 2,
  },
  petroleumBadgeText: {
    fontFamily: FONTS.titleCondensed,
    fontWeight: '700',
    fontSize: 11,
    color: COLORS.paper,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  dividerLine: {
    height: 1,
    backgroundColor: COLORS.steelGray,
    marginVertical: 10,
    opacity: 0.5,
  },
  dividerDouble: {
    height: 3,
    borderTopWidth: 1,
    borderBottomWidth: 1,
    borderColor: COLORS.softBlack,
    marginVertical: 12,
  },
  buttonPrimary: {
    backgroundColor: COLORS.brickRed,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    paddingVertical: 14,
    paddingHorizontal: 20,
    borderRadius: 2,
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    gap: 8,
  },
  buttonPrimaryText: {
    fontFamily: FONTS.titleCondensed,
    fontWeight: '800',
    fontSize: 16,
    color: COLORS.paper,
    textTransform: 'uppercase',
    letterSpacing: 1.2,
  },
  buttonSecondary: {
    backgroundColor: COLORS.paper,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderRadius: 2,
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    gap: 8,
  },
  buttonSecondaryText: {
    fontFamily: FONTS.titleCondensed,
    fontWeight: '700',
    fontSize: 14,
    color: COLORS.softBlack,
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
});
