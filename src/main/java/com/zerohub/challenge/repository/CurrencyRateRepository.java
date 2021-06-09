package com.zerohub.challenge.repository;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.zerohub.challenge.models.CurrencyPair;
import com.zerohub.challenge.models.CurrencyRateException;
import com.zerohub.challenge.graph_search.DijkstraWithPriorityQueue;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CurrencyRateRepository {
  private final static int CURRENCY_SCALE = 9;
  private final static RoundingMode CURRENCY_ROUNDING = RoundingMode.HALF_UP;
  private final Map<CurrencyPair, BigDecimal> storage;
  // TODO: can be reworked using own impl for graph.
  private final MutableValueGraph<String, Integer> graph;

  public CurrencyRateRepository() {
    storage = new HashMap<>();
    graph = ValueGraphBuilder.undirected().build();
  }

  public void addCurrencyRate(final String baseCurrency, final String pairCurrency, final String rawRate) throws CurrencyRateException {
    if (baseCurrency.equals(pairCurrency)) {
      throw new CurrencyRateException("Base and pair currency cannot be equal.");
    }
    var rate = new BigDecimal(rawRate);
    if (rate.doubleValue() < 10E-9) {
      throw new CurrencyRateException("Unable to add currency rate. Rate cannot be that low.");
    }
    storage.put(CurrencyPair.builder().base(baseCurrency).pair(pairCurrency).build(), rate);
    storage.put(CurrencyPair.builder().base(pairCurrency).pair(baseCurrency).build(),
      (new BigDecimal(1)).setScale(CURRENCY_SCALE, CURRENCY_ROUNDING).divide(rate, CURRENCY_ROUNDING));
    graph.putEdgeValue(baseCurrency, pairCurrency, 1);
  }

  public BigDecimal getCurrencyRate(final String baseCurrency, final String pairCurrency) throws CurrencyRateException {
    if (baseCurrency.equals(pairCurrency)) {
      return new BigDecimal(1);
    }
    List<String> path = DijkstraWithPriorityQueue.findShortestPath(graph, baseCurrency, pairCurrency);
    if (null == path) {
      throw new CurrencyRateException("No rate found for this currencies.");
    }
    var rate = new BigDecimal(1);
    for (int i = 0; i < path.size() - 1; i++) {
      var curRate = storage.get(
        CurrencyPair.builder().base(path.get(i)).pair(path.get(i + 1)).build()
      );
      rate = rate.setScale(CURRENCY_SCALE, CURRENCY_ROUNDING).multiply(curRate);
    }
    return rate;
  }
}
