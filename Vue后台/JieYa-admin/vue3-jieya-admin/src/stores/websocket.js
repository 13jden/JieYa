import { defineStore } from 'pinia'

export const useWebsocketStore = defineStore('websocket', {
  state: () => ({
    connected: false
  }),
  actions: {
    setConnected(status) {
      this.connected = status
    }
  }
}) 