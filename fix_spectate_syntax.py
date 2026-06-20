import os

def fix_event():
    f = "/Users/aleksei.chernousov/IdeaProjects/igaming/igaming-source-888sport/src/main/java/pro/datawiki/igaming/source/sport888/dto/spectate/SpectateEvent.java"
    with open(f, 'r') as file:
        content = file.read()
    content = content.replace("public Map<String, getSpectateMarket>() { return SpectateMarket>; }", "public Map<String, SpectateMarket> getMarkets() { return markets; }")
    content = content.replace("public void setSpectateMarket>(Map<String, SpectateMarket>) { this.SpectateMarket> = SpectateMarket>; }", "public void setMarkets(Map<String, SpectateMarket> markets) { this.markets = markets; }")
    with open(f, 'w') as file:
        file.write(content)

def fix_response():
    f = "/Users/aleksei.chernousov/IdeaProjects/igaming/igaming-source-888sport/src/main/java/pro/datawiki/igaming/source/sport888/dto/spectate/SpectateEventsResponse.java"
    with open(f, 'r') as file:
        content = file.read()
    content = content.replace("public List<getSpectateEvent>() { return SpectateEvent>; }", "public List<SpectateEvent> getEvents() { return events; }")
    content = content.replace("public void setSpectateEvent>(List<SpectateEvent>) { this.SpectateEvent> = SpectateEvent>; }", "public void setEvents(List<SpectateEvent> events) { this.events = events; }")
    with open(f, 'w') as file:
        file.write(content)

def fix_market():
    f = "/Users/aleksei.chernousov/IdeaProjects/igaming/igaming-source-888sport/src/main/java/pro/datawiki/igaming/source/sport888/dto/spectate/SpectateMarket.java"
    with open(f, 'r') as file:
        content = file.read()
    content = content.replace("public Map<String, getSpectateSelection>() { return SpectateSelection>; }", "public Map<String, SpectateSelection> getSelections() { return selections; }")
    content = content.replace("public void setSpectateSelection>(Map<String, SpectateSelection>) { this.SpectateSelection> = SpectateSelection>; }", "public void setSelections(Map<String, SpectateSelection> selections) { this.selections = selections; }")
    with open(f, 'w') as file:
        file.write(content)

fix_event()
fix_response()
fix_market()

