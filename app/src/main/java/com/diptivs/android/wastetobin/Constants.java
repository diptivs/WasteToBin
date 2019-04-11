package com.diptivs.android.wastetobin;

public class Constants {

  public static final String TAGDipti = "dipti";
  /** Number of categories that the city of Toronto supports.*/
  public static final int CATEGORIES_SIZE = 10;
  /** Maximum number of similar entries to be returned when checking for similar words.*/
  public static final int LIMIT = 20;
  /** List of supported languages.*/
  public static final String[] supportedCategoriesLanguages = {"English", "Hindi"};
  /** Collection of categories defined by the city of toronto's recycling council.*/
  public enum Categories{
    /** Blue Bin. */
    BLUE_BIN,
    /** Bring to Transfer Station or Waste Depot. */
    BRING_TO_TRANSFER_STATION_OR_WASTE_DEPOT,
    /** E-Waste.*/
    E_WASTE,
    /** Green Bin.*/
    GREEN_BIN,
    /** Grey Bin - Garbage.*/
    GREY_BIN ,
    /** Household Hazardous Waste.*/
    HOUSEHOLD_HAZARDOUS_WASTE,
    /** Over-sized Waste.*/
    OVERSIZED_WASTE,
    /** Prohibited Waste.*/
    PROHIBITED_WASTE,
    /** Scrap Metal.*/
    SCRAP_METAL,
    /** Yard Waste.*/
    YARD_WASTE
  }
  /** Respective string representation for variables in {@link Categories}*/
  public static String[] categoriesName = {"Blue Bin", "Transfer Station or Waste Depot",
          "E-Waste", "Green Bin", "Grey Bin - Garbage",
          "Household Hazardous Waste", "Oversized Waste",
          "Prohibited Waste", "Scrap Metal", "Yard Waste"};
  /** State for image capture.*/
  public static final int REQ_CAPTURE_IMAGE = 0;
  /** State for voice recognition. */
  public static final int REQ_VOICE = 1;
  /** State for image capture.*/
  public static final int REQUEST_IMAGE_CAPTURE = 2;

}
