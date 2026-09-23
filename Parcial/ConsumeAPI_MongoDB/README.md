# 📑 Chuleta Rápida de Ngrok (`npm` / `npx`)

Guía rápida de comandos para exponer tu servidor local de **Express** a internet y consumirlo desde **Expo Go**.

---

## 🔑 1. Configuración Inicial (Solo una vez)

Guarda tu token de autenticación de forma permanente en tu computadora para no tener que escribirlo nunca más:

```bash
npx ngrok config add-authtoken TU_AUTHTOKEN_AQUÍ
```

---

## ⚡ 2. Activación del Túnel (Uso diario)

Abre el túnel público apuntando al puerto específico donde corre tu servidor de Express (por ejemplo, el puerto `4000` o `3000`). Tienes que correr antes tu servidor con npm server.js o similar.

```bash
npx ngrok http 4000
```

---

## 🚀 3. Comando "Todo en Uno" (Línea directa)

Si estás trabajando en otra computadora y quieres activar el túnel inyectando el token en una sola línea (sin guardar nada en el sistema):

```bash
npx ngrok http 4000 --authtoken TU_AUTHTOKEN_AQUÍ
```

---

## 📦 4. Automatización desde Express (SDK Oficial)

Si prefieres que el túnel se abra de forma automática al encender tu servidor Express sin necesidad de abrir una segunda terminal:

### Paso A: Instala el SDK en tu API
```bash
npm install @ngrok/ngrok
```

### Paso B: Integra el código en tu archivo de Express (`server.js`)
```javascript
const express = require('express');
const ngrok = require('@ngrok/ngrok');

const app = express();
const PORT = 4000;

app.listen(PORT, async () => {
  console.log(`Servidor local en http://localhost:${PORT}`);
  
  try {
    // Activa el túnel automáticamente usando el token guardado en el sistema
    const session = await ngrok.forward({ addr: PORT, authtoken_from_env: true });
    console.log(`🔗 URL pública para Expo Go: ${session.url()}`);
  } catch (error) {
    console.error('Error al iniciar Ngrok:', error);
  }
});
```

---

## ⚠️ Recuerda para tu App de Expo Go

Para evitar que la página de advertencia del plan gratuito de Ngrok rompa tus peticiones `fetch` o `axios`, incluye **siempre** este encabezado en tu aplicación de React Native:

```javascript
headers: {
  'Accept': 'application/json',
  'Content-Type': 'application/json',
  'ngrok-skip-browser-warning': 'true' // 🌟 Evita el bloqueo de Ngrok
}
```
