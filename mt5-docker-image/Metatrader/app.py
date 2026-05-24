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

@app.get("/price/{symbol}")
def get_price(symbol: str):
    tick = mt5.symbol_info_tick(symbol)
    if not tick:
        raise HTTPException(status_code=404, detail="Ativo não encontrado")
    return {"symbol": symbol, "bid": tick.bid, "ask": tick.ask}

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