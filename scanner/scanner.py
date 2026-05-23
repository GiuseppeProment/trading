import MetaTrader5 as mt5
import pandas as pd
import pandas_ta as ta
import time
from datetime import datetime

# --- CONFIGURAÇÕES ---
# Coloque aqui os papéis que você quer monitorar (ativos da B3 precisam de sufixo se houver)
ATIVOS = ["PETR4", "VALE3", "WINM26", "WDOM26", "ITUB4"] 

path_mt5 = "/home/giuseppe/.mt5/drive_c/Program Files/MetaTrader 5/terminal64.exe"

def get_data(symbol, timeframe, n=100):
    rates = mt5.copy_rates_from_pos(symbol, timeframe, 0, n)
    if rates is None or len(rates) == 0:
        return pd.DataFrame()
    df = pd.DataFrame(rates)
    df['time'] = pd.to_datetime(df['time'], unit='s')
    return df

def check_confluence(symbol):
    try:
        # Pega dados de 1 min e 5 min
        df1 = get_data(symbol, mt5.TIMEFRAME_M1)
        df5 = get_data(symbol, mt5.TIMEFRAME_M5)

        if df1.empty or df5.empty:
            return False

        def get_signals(df):
            # EMA 9 e VWAP
            ema = ta.ema(df['close'], length=9)
            # Nota: VWAP em scanners de tempo real depende do volume do dia
            vwap = ta.vwap(df['high'], df['low'], df['close'], df['tick_volume'])
            
            # Estocástico (K=14, D=3, Smooth=3)
            stoch = ta.stoch(df['high'], df['low'], df['close'])
            k = stoch['STOCHk_14_3_3']
            d = stoch['STOCHd_14_3_3']

            # Verifica cruzamento na última vela fechada (índice -2) para a atual (-1)
            # Cruzamento de compra: EMA cruza pra cima da VWAP E K cruza pra cima de D
            buy_cross = (ema.iloc[-2] < vwap.iloc[-2] and ema.iloc[-1] > vwap.iloc[-1]) and \
                        (k.iloc[-2] < d.iloc[-2] and k.iloc[-1] > d.iloc[-1])
            
            return buy_cross

        # Só retorna True se a condição bater nos dois tempos gráficos
        return get_signals(df1) and get_signals(df5)
    except Exception as e:
        return False



# --- LOOP PRINCIPAL ---
if not mt5.initialize(path=path_mt5):
    print("Erro ao inicializar MT5")
    quit()

print(f"[{datetime.now()}] Scanner Iniciado. Monitorando: {len(ATIVOS)} ativos...")

try:
    while True:
        # Lista para armazenar apenas quem deu sinal
        lista_limpa = []

        for s in ATIVOS:
            # Garante que o ativo está visível no Market Watch
            mt5.symbol_select(s, True)
            
            if check_confluence(s):
                lista_limpa.append(s)

        # Output Limpo
        if lista_limpa:
            print("\a") # Toca um "beep" do sistema se houver sinal
            print(f"--- SINAL ENCONTRADO EM: {datetime.now().strftime('%H:%M:%S')} ---")
            for item in lista_limpa:
                print(f"| [!] {item} - Confluência 1' e 5' confirmada!")
            print("-" * 40)
        
        # Sleep de 10 segundos para não sobrecarregar a CPU e o Wine
        time.sleep(10)

except KeyboardInterrupt:
    print("Scanner encerrado.")
    mt5.shutdown()