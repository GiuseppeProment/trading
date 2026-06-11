import MetaTrader5 as mt5
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI()

# Inicializa o MT5 assim que a API REST sobe
if not mt5.initialize():
    print("Falha ao inicializar o MT5")

class OrderSchema(BaseModel):
    symbol: str
    volume: float
    order_type: str # 'BUY' ou 'SELL'

@app.get("/symbol_info_tick/{symbol}")
def symbol_info_tick(symbol: str):
    tick = mt5.symbol_info_tick(symbol)
    if tick is None:
        raise HTTPException(status_code=404, detail="Ativo não encontrado")
    return tick._asdict()

@app.get("/symbol_info/{symbol}")
def symbol_info(symbol: str):
    symbol_info = mt5.symbol_info(symbol)
    if symbol_info is None:
        raise HTTPException(status_code=404, detail="Ativo não encontrado ["+mt5.last_error()+"]")
    return symbol_info._asdict()

@app.get("/symbols_get/{group}")
def symbols_get(group: str):
    result = mt5.symbols_get(group)
    if result is None:
        raise HTTPException(status_code=404, detail=mt5.last_error())
    return [row._asdict() for row in result]

@app.get("/copy_rates_from_pos/{symbol}/{timeframe}/{start_pos}/{count}")
def copy_rates_from_pos(symbol: str, timeframe: str, start_pos: int, count: int):
    result = mt5.copy_rates_from_pos(symbol, TIMEFRAME_MAPPING[timeframe], start_pos, count)
    if result is None:
        raise HTTPException(status_code=404, detail=mt5.last_error())
    result_as_dict = []
    for row in result:
        result_as_dict.append({
            "time": int(row['time']),
            "open": float(row['open']),
            "high": float(row['high']),
            "low": float(row['low']),
            "close": float(row['close']),
            "tick_volume": int(row['tick_volume']),
            "spread": int(row['spread']),
            "real_volume": int(row['real_volume'])})
    return result_as_dict


TIMEFRAME_MAPPING = {
    "M1": mt5.TIMEFRAME_M1,
    "M2": mt5.TIMEFRAME_M2,
    "M3": mt5.TIMEFRAME_M3,
    "M4": mt5.TIMEFRAME_M4,
    "M5": mt5.TIMEFRAME_M5,
    "M6": mt5.TIMEFRAME_M6,
    "M10": mt5.TIMEFRAME_M10,
    "M12": mt5.TIMEFRAME_M12,
    "M15": mt5.TIMEFRAME_M15,
    "M20": mt5.TIMEFRAME_M20,
    "M30": mt5.TIMEFRAME_M30,
    "H1": mt5.TIMEFRAME_H1,
    "H2": mt5.TIMEFRAME_H2,
    "H3": mt5.TIMEFRAME_H3,
    "H4": mt5.TIMEFRAME_H4,
    "H6": mt5.TIMEFRAME_H6,
    "H8": mt5.TIMEFRAME_H8,
    "H12": mt5.TIMEFRAME_H12,
    "D1": mt5.TIMEFRAME_D1,
    "W1": mt5.TIMEFRAME_W1,
    "MN1": mt5.TIMEFRAME_MN1
}

@app.post("/order")
def enviar_ordem(order: OrderSchema):
    # Lógica nativa do Python-MT5 para enviar ordens
    tipo = mt5.ORDER_TYPE_BUY if order.order_type == "BUY" else mt5.ORDER_TYPE_SELL
    
    request = {
        "action": mt5.TRADE_ACTION_DEAL,
        "symbol": order.symbol,
        "volume": order.volume,
        "type": tipo,
        "price": mt5.symbol_info_tick(order.symbol).ask if order.order_type == "BUY" else mt5.symbol_info_tick(order.symbol).bid,
        "deviation": 20,
        "magic": 234000,
        "comment": "API Wrapper REST",
        "type_time": mt5.ORDER_TIME_GTC,
        "type_filling": mt5.ORDER_FILLING_RETURN,
    }
    
    result = mt5.order_send(request)
    return {"retcode": result.retcode, "order_id": result.order}
