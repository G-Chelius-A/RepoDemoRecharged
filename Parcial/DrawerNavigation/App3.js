import { View, Text } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';

'@react-navigation/native';

import { createDrawerNavigator } from '@react-navigation/drawer';

const Drawer = createDrawerNavigator();

function HomeScreen() {
  return (
    <View>
      <Text>Inicio</Text>
    </View>
  );
}

function SearchScreen(){
  return(
    <View>
      <Text>Buscar</Text>
    </View>
  );
}

function ProfileScren() {
  return(
    <View>
      <Text>Perfil</Text>
    </View>
  );
}

function SettingsScreen() {
  return(
    <View>
      <Text>Ajustes</Text>
    </View>
  );
}

function MyDrawer() {
  return(
    <Drawer.Navigator>
      <Drawer.Screen 
        name="Inicio"
        component={HomeScreen}
      />
      <Drawer.Screen 
        name="Buscar"
        component={SearchScreen}
      />
      <Drawer.Screen 
        name="Perfil"
        component={ProfileScren}
      />
      <Drawer.Screen 
        name="Ajustes"
        component={SettingsScreen}
      />
    </Drawer.Navigator>
  );
}

export default function App(){
  return(
    <NavigationContainer>
      <MyDrawer/>
    </NavigationContainer>
  );
}