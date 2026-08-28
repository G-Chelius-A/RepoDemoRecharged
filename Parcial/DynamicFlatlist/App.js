import { use, useState } from 'react';
import { StyleSheet, Text, View, FlatList, TouchableOpacity } from 'react-native';
import CustomModal from './components/CustomModal';

const cursos = [
  {id: '1', titulo: 'Aplicaciones Móviles', duracion:'20 hrs.', rating:'5.0',},
  {id: '2', titulo: 'Base de Datos', duracion:'30 hrs.', rating:'4.0',},
  {id: '3', titulo: 'Redes de computadoras', duracion:'40 hrs.', rating:'4.6',},
  {id: '4', titulo: 'Desarrollo de aplicaciones web', duracion:'20 hrs.', rating:'4.8',},
  {id: '5', titulo: 'Ciberseguridad', duracion:'30 hrs.', rating:'4.3',},
]

export default function App() {

  const [modalVisible, setModalVisible] = useState(false);
  const [cursoSeleccionado, setCursoSeleccionado] = useState(null);
  const objeto = {valor: cursoSeleccionado, };

  const manejaPresionCurso = (tituloCurso) => {
    setCursoSeleccionado({valor: tituloCurso})
    setModalVisible(true);
  }

  const renderCard = ({item}) => (
    <TouchableOpacity 
      style={styles.card}
      onPress={()=>manejaPresionCurso(item.titulo)}
      activeOpacity={0.7}
    >
      <View>
        <Text style={styles.title}>{item.titulo}</Text>
        <Text style={styles.subtitulo}>{item.duracion} | ⭐ {item.rating}</Text>
      </View>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <CustomModal
        visible={modalVisible}
        onClose={()=>setModalVisible(false)}
        contenido={cursoSeleccionado}
      />

      <Text style={styles.header}> Mi Lista de Cursos </Text>
      <FlatList
        data={cursos}
        renderItem={renderCard}
        keyExtractor={item=>item.id}
        contentContainerStyle={styles.listContainer}
      />      
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  listContainer: {
    paddingHorizontal: 16,
    paddingBottom: 16,
  },
  header: {
    fontSize: 24,
    fontWeight: 'bold',
    padding: 16,
    color: '#1a1a1a'
  },
  card: {
    backgroundColor: '#ddd',
    padding: 16,
    marginVertical: 8,
    borderRadius: 12,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 4,
  },
  subtitulo: {
    fontSize: 14,
    color: '#777',
  },
});
