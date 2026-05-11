#!/bin/bash

# --- FARBEN ---
G="\e[0;32m"; C="\e[0;36m"; Y="\e[0;33m"; R="\e[0;31m"; W="\e[0;37m"; M="\e[0;35m"; B="\e[1m"; NC="\033[0m"

# --- PFADE (Vom Setup-Script injiziert) ---
VENV_PATH_VAL="/data/data/jkas.androidpe/files/home/.venv"
SECRET_GEMINI="/data/data/jkas.androidpe/files/home/.gemini_api_key.secrets"
SECRET_ANTHROPIC="/data/data/jkas.androidpe/files/home/.anthropic_api_key.secrets"
SECRET_OPENAI="/data/data/jkas.androidpe/files/home/.openai_api_key.secrets"
SECRET_DEEPSEEK="/data/data/jkas.androidpe/files/home/.deepseek_api_key.secrets"

# --- GLOBALE STATUS-VARIABLEN ---
SUBTREE_MODE=false

# Arrays für Chat-Modes
CHAT_MODES=("auto" "code" "architect" "ask" "help")
CHAT_MODE_DESCS=(
    "Launcher-Empfehlung (Standard)" 
    "Direktes Coden (am besten für Flash)" 
    "Planen & Coden (am besten für Pro/Komplexes)" 
    "Nur Fragen stellen (keine Dateiänderungen)" 
    "Fragen zur Bedienung von Aider"
)
CHAT_MODE_IDX=0

load_secrets() {
    export UV_LINK_MODE=copy
    [ -f "$SECRET_GEMINI" ] && export GEMINI_API_KEY=$(cat "$SECRET_GEMINI" | xargs)
    [ -f "$SECRET_ANTHROPIC" ] && export ANTHROPIC_API_KEY=$(cat "$SECRET_ANTHROPIC" | xargs)
    [ -f "$SECRET_OPENAI" ] && export OPENAI_API_KEY=$(cat "$SECRET_OPENAI" | xargs)
    [ -f "$SECRET_DEEPSEEK" ] && export DEEPSEEK_API_KEY=$(cat "$SECRET_DEEPSEEK" | xargs)
}

# ==========================================
# START-LOGIK
# ==========================================
run_aider() {
    local model=$1
    local mode_flag=$2
    
    echo -e "${G}Starte Aider mit Modell: ${W}$model ${mode_flag}${NC}"
    source "$VENV_PATH_VAL/bin/activate"
    
    if [ -z "$mode_flag" ]; then
        aider --model "$model"
    else
        aider --model "$model" $mode_flag
    fi
    
    echo -e "${Y}Aider beendet. Drücke Enter, um ins Menü zurückzukehren.${NC}"
    read
}

choose_run_mode() {
    local model=$1
    local default_mode=$2
    
    echo -e "\n${W}Wie möchtest du starten?${NC}"
    echo -e "1) Standard (Normales Coding)"
    if [ "$default_mode" == "--architect" ]; then
        echo -e "2) Architect Mode (Besser für komplexe Architektur/Refactoring)"
    fi
    echo -e "0) Abbrechen"
    read -p "Wahl: " mode_choice
    
    case $mode_choice in
        1) run_aider "$model" "" ;;
        2) 
            if [ "$default_mode" == "--architect" ]; then
                run_aider "$model" "--architect"
            else
                echo -e "${R}Ungültige Wahl.${NC}"; sleep 1
            fi
            ;;
        0) return ;;
        *) echo -e "${R}Ungültige Eingabe.${NC}"; sleep 1 ;;
    esac
}

# ==========================================
# MODELLE AUFLISTEN LOGIK
# ==========================================
list_aider_models() {
    local search_term=$1
    echo -e "\n${C}Lade verfügbare Modelle für: ${W}${search_term}${NC}"
    source "$VENV_PATH_VAL/bin/activate"
    # Aider wird mit dem Suchbegriff aufgerufen, um alle passenden Modelle aufzulisten
    aider --models "$search_term"
    echo -e "\n${Y}Suche beendet. Drücke Enter, um zurückzukehren.${NC}"
    read
}

menu_list_models() {
    while true; do
        clear
        echo -e "${M}======================================================${NC}"
        echo -e "${W}${B}         MODELLE AUFLISTEN                         ${NC}"
        echo -e "${M}======================================================${NC}"
        echo -e "${W}1) List OpenAI Models${NC}"
        echo -e "${G}2) List Anthropic Models${NC}"
        echo -e "${C}3) List Gemini Models${NC}"
        echo -e "${Y}4) List Deepseek Models${NC}"
        echo -e "------------------------------------------------------"
        echo -e "${R}0) Zurück${NC}"
        read -p "Wahl: " list_choice
        case $list_choice in
            1) list_aider_models "openai/" ;;
            2) list_aider_models "anthropic/" ;;
            3) list_aider_models "gemini/" ;;
            4) list_aider_models "deepseek/" ;;
            0) return ;;
            *) echo -e "${R}Ungültige Eingabe.${NC}"; sleep 1 ;;
        esac
    done
}

# ==========================================
# UNTERMENÜS FÜR GEMINI (PRO / FLASH)
# ==========================================

menu_gemini_pro() {
    while true; do
        clear
        echo -e "${C}======================================================${NC}"
        echo -e "${W}${B}         GEMINI PRO MODELLE                        ${NC}"
        echo -e "${C}======================================================${NC}"
        echo -e "${C}1) Gemini 2.0 Pro Exp${NC}   (Experimentell: Höchste Intelligenz)"
        echo -e "${C}2) Gemini 1.5 Pro${NC}       (2M Tokens - Stabil für riesige Refactorings)"
        echo -e "------------------------------------------------------"
        echo -e "${R}0) Zurück${NC}"
        read -p "Wahl: " gem_pro
        case $gem_pro in
            1) choose_run_mode "gemini/gemini-2.0-pro-exp-02-05" "--architect" ;;
            2) choose_run_mode "gemini/gemini-1.5-pro-002" "--architect" ;;
            0) return ;;
            *) echo -e "${R}Ungültige Eingabe.${NC}"; sleep 1 ;;
        esac
    done
}

menu_gemini_flash() {
    while true; do
        clear
        echo -e "${C}======================================================${NC}"
        echo -e "${W}${B}         GEMINI FLASH MODELLE                      ${NC}"
        echo -e "${C}======================================================${NC}"
        echo -e "${C}1) Gemini 2.0 Flash${NC}          (Der neue schnelle & smarte Standard)"
        echo -e "${C}2) Gemini 2.0 Flash Thinking${NC} (Experimentell: Deep Reasoning für Code)"
        echo -e "${C}3) Gemini 1.5 Flash${NC}          (1M/2M Tokens - Extrem schnell & stabil)"
        echo -e "${C}4) Gemini 1.5 Flash-8B${NC}       (Blitzschnell, für kleine Tasks)"
        echo -e "${C}5) Gemini Flash Lite${NC}         (Standard/Ressourcenschonend)"
        echo -e "------------------------------------------------------"
        echo -e "${R}0) Zurück${NC}"
        read -p "Wahl: " gem_flash
        case $gem_flash in
            1) choose_run_mode "gemini/gemini-2.0-flash" "--architect" ;;
            2) choose_run_mode "gemini/gemini-2.0-flash-thinking-exp-01-21" "" ;;
            3) choose_run_mode "gemini/gemini-1.5-flash-002" "--architect" ;;
            4) choose_run_mode "gemini/gemini-1.5-flash-8b" "" ;;
            5) choose_run_mode "gemini/gemini-flash-lite-latest" "" ;;
            0) return ;;
            *) echo -e "${R}Ungültige Eingabe.${NC}"; sleep 1 ;;
        esac
    done
}

menu_gemini() {
    while true; do
        clear
        echo -e "${C}======================================================${NC}"
        echo -e "${W}${B}         GEMINI MODELLE (Google)                   ${NC}"
        echo -e "${C}======================================================${NC}"
        echo -e "Tipp: Für das große Refactoring nutze 2.0 Flash oder 1.5 Pro!"
        echo ""
        echo -e "${C}1) Gemini Pro Modelle${NC}   (Für komplexe Aufgaben & tiefes Verständnis)"
        echo -e "${C}2) Gemini Flash Modelle${NC} (Für riesigen Kontext & maximale Geschwindigkeit)"
        echo -e "------------------------------------------------------"
        echo -e "${R}0) Zurück${NC}"
        read -p "Wahl: " gem
        case $gem in
            1) menu_gemini_pro ;;
            2) menu_gemini_flash ;;
            0) return ;;
            *) echo -e "${R}Ungültige Eingabe.${NC}"; sleep 1 ;;
        esac
    done
}

# ==========================================
# WEITERE UNTERMENÜS FÜR MODELLE ZUM STARTEN
# ==========================================

menu_openai() {
    while true; do
        clear
        echo -e "${W}======================================================${NC}"
        echo -e "${W}${B}         OPENAI MODELLE                            ${NC}"
        echo -e "${W}======================================================${NC}"
        echo -e "${W}1) GPT-4o${NC}       (Bestes Allround-Modell)"
        echo -e "${W}2) GPT-4o-mini${NC}  (Schnell & Günstig)"
        echo -e "${W}3) o1${NC}           (Deep Reasoning - Finale Version)"
        echo -e "${W}4) o3-mini${NC}      (Neuestes Coding Reasoning Modell)"
        echo -e "${W}5) o1-preview${NC}   (Älteres Reasoning Modell)"
        echo -e "${W}6) o1-mini${NC}      (Älteres Mini Reasoning)"
        echo -e "------------------------------------------------------"
        echo -e "${R}0) Zurück${NC}"
        read -p "Wahl: " oai
        case $oai in
            1) choose_run_mode "gpt-4o" "--architect" ;;
            2) choose_run_mode "gpt-4o-mini" "" ;;
            3) choose_run_mode "o1" "" ;;
            4) choose_run_mode "o3-mini" "" ;;
            5) choose_run_mode "o1-preview" "" ;;
            6) choose_run_mode "o1-mini" "" ;;
            0) return ;;
            *) echo -e "${R}Ungültige Eingabe.${NC}"; sleep 1 ;;
        esac
    done
}

menu_deepseek() {
    while true; do
        clear
        echo -e "${Y}======================================================${NC}"
        echo -e "${W}${B}         DEEPSEEK MODELLE                          ${NC}"
        echo -e "${Y}======================================================${NC}"
        echo -e "${Y}1) DeepSeek R1 (Reasoner)${NC} (Das revolutionäre Deep-Thinking Modell!)"
        echo -e "${Y}2) DeepSeek V3 (Chat)${NC}     (Pfeilschnell und extrem smart für Code)"
        echo -e "${Y}3) DeepSeek Coder V2${NC}      (Älteres Code Modell)"
        echo -e "------------------------------------------------------"
        echo -e "${R}0) Zurück${NC}"
        read -p "Wahl: " ds
        case $ds in
            1) choose_run_mode "deepseek/deepseek-reasoner" "" ;;
            2) choose_run_mode "deepseek/deepseek-chat" "--architect" ;;
            3) choose_run_mode "deepseek/deepseek-coder" "" ;;
            0) return ;;
            *) echo -e "${R}Ungültige Eingabe.${NC}"; sleep 1 ;;
        esac
    done
}

menu_claude() {
    while true; do
        clear
        echo -e "${G}======================================================${NC}"
        echo -e "${W}${B}         CLAUDE MODELLE (Anthropic)                ${NC}"
        echo -e "${G}======================================================${NC}"
        echo -e "${G}1) Claude 3.7 Sonnet${NC} (BRANDNEU: Absoluter Coding-König!)"
        echo -e "${G}2) Claude 3.5 Sonnet${NC} (Der bewährte Vorgänger)"
        echo -e "${G}3) Claude 3.5 Haiku${NC}  (Schnell & Günstig)"
        echo -e "${G}4) Claude 3 Opus${NC}     (Creative Reasoning)"
        echo -e "------------------------------------------------------"
        echo -e "${R}0) Zurück${NC}"
        read -p "Wahl: " cl
        case $cl in
            1) choose_run_mode "claude-3-7-sonnet-20250219" "--architect" ;;
            2) choose_run_mode "claude-3-5-sonnet-20241022" "--architect" ;;
            3) choose_run_mode "claude-3-5-haiku-20241022" "" ;;
            4) choose_run_mode "claude-3-opus-20240229" "--architect" ;;
            0) return ;;
            *) echo -e "${R}Ungültige Eingabe.${NC}"; sleep 1 ;;
        esac
    done
}

# ==========================================
# HAUPTMENÜ
# ==========================================

while true; do
    clear
    echo -e "${C}======================================================${NC}"
    echo -e "${W}${B}         AIDER AI - HAUPTMENÜ                      ${NC}"
    echo -e "${C}======================================================${NC}"
    load_secrets

    echo -e "${C}1) Gemini${NC}    (inkl. 2.0 Flash & 1.5 Pro)"
    echo -e "${W}2) OpenAI${NC}    (inkl. o3-mini & GPT-4o)"
    echo -e "${Y}3) DeepSeek${NC}  (inkl. DeepSeek R1)"
    echo -e "${G}4) Claude${NC}    (inkl. Claude 3.7 Sonnet)"
    echo -e "------------------------------------------------------"
    echo -e "${M}5) List Models${NC} (Zeigt verfügbare Modelle der API)"
    echo -e "------------------------------------------------------"
    echo -e "${R}6) Beenden${NC}"
    read -p "Wahl: " main
    case $main in
        1) menu_gemini ;;
        2) menu_openai ;;
        3) menu_deepseek ;;
        4) menu_claude ;;
        5) menu_list_models ;;
        6|0) echo -e "${G}Tschüss!${NC}"; exit 0 ;;
        *) echo -e "${R}Ungültige Eingabe.${NC}"; sleep 1 ;;
    esac
done