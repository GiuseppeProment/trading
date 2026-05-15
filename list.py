import MetaTrader5 as mt5
import pandas as pd

def initialize():
    if not mt5.initialize("/home/giuseppe/.mt5/drive_c/Program Files/MetaTrader 5/terminal64.exe"):
        print(f"Falha ao iniciar MT5: {mt5.last_error()}")

def filter_stocks_mt5_mode(symbols):
    return [s.name for s in symbols
            if s.option_mode == 0 and               # não é opção
               s.trade_calc_mode == 32 and          # modo de cálculo para ações
               s.trade_mode == 4 and                # long e short permitidos
               not s.name.endswith('F') and         # não é fracionária
            "BOVESPA" in s.path         and         # esta na bovespa
            s.name.endswith(('3','4','5','34')) and # terminação em 3/4/5/34 
            not s.name.startswith(('TF','TAXA'))    # não é taxa ou tesouro
    ]

def main():
    initialize()
    symbols = mt5.symbols_get()
    print(f"Total de símbolos encontrados: {len(symbols)}")
    symbols = filter_stocks_mt5_mode(symbols)
    print(f"Total de ações filter_stocks_mt5_mode : {len(symbols)}")
    symbols = filter_low_spread(symbols)
    print(f"Total de ações com low spread: {len(symbols)}")    
    symbols = filter_by_volume(symbols)
    print(f"Total de ações com volume financeiro relevante: {len(symbols)}")    
    print(symbols)
    mglu3 = mt5.symbol_info("MGLU3")
    #print(mglu3._asdict())
    show_market_watch(symbols)
    mt5.shutdown()

def show_market_watch(symbols):
    mt5.symbol_select("*", False) 
    for s in symbols:
        mt5.symbol_select(s, True)


def filter_by_volume(lista_simbolos, volume_minimo_financeiro=20_000_000):
    simbolos_liquidos = []
    print("Analisando volume:")
    for s in lista_simbolos:
        print(".", sep="",end="")
        rates = mt5.copy_rates_from_pos(s, mt5.TIMEFRAME_D1, 0, 1)
        if rates is not None and len(rates) > 0:
            vol_real = rates[0]['real_volume'] if rates[0]['real_volume'] > 0 else rates[0]['tick_volume']
            preco = rates[0]['close']
            vol_financeiro = vol_real * preco

            if vol_financeiro >= volume_minimo_financeiro:
                simbolos_liquidos.append(s)
    print("Volumes analisados")
    return simbolos_liquidos

def filter_low_spread(simbols, spread_max_centavos=2):
    
    simbols_result = []
    for ticker in simbols:
        info = mt5.symbol_info(ticker)
        if info is None: 
            continue
        spread_atual = info.spread 
        # Filtro Simples: Se o spread for maior que 2 centavos, descarta.
        # Isso elimina na hora ações de "segunda linha" e micos.
        if spread_atual > spread_max_centavos:
            continue
        # Filtro Percentual (Opcional - mais rigoroso)
        preco_venda = info.ask
        if preco_venda > 0:
            percentual_spread = (spread_atual * 0.01) / preco_venda
            if percentual_spread > 0.0015: # 0.15%
                continue
            
        simbols_result.append(ticker)    
    return simbols_result

if __name__ == "__main__":
    main()